package neilw4.omin.crypto
import android.app.Activity
import android.os.Bundle

import it.unisa.dia.gas.crypto.jpbc.fe.ibe.lw11.engines.{UHIBELW11PredicateOnlyEngine, UHIBELW11KEMEngine}
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.lw11.generators.UHIBELW11KeyPairGenerator
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.lw11.generators.UHIBELW11SecretKeyGenerator
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.lw11.params._
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory

   import java.util.Arrays

import org.bouncycastle.crypto.params.ParametersWithRandom

// Adapted from http://gas.dia.unisa.it/projects/jpbc/schemes/uhibe_lw11.html#.VGIMY3WsXeQ
object KeyGen {
    def test() = {
        val (masterPublic, masterPrivate) = setup(32)
        val aId = something(masterPublic, "a")
        val aPrivate = keyGen(masterPublic, masterPrivate, aId)
        val mEnc = encrypt("msg", masterPublic, aId)
        val mPlain = decrypt(mEnc, aPrivate)
        val msgBytes = "msg".getBytes
        android.util.Log.e("T--", Arrays.toString(msgBytes))
        android.util.Log.e("T--", Arrays.toString(mPlain))
        assert(Arrays.equals(msgBytes, mPlain))
    }

       def test1() = {
           val (masterPublic, masterPrivate) = setup(32)
           val aId = something(masterPublic, "a")
           val msgElement = something(masterPublic, "")
           val aPrivate = keyGen(masterPublic, masterPrivate, aId)
           val (mKey, mEnc) = encaps(masterPublic, aId, msgElement)
           val mPlain = decaps(aPrivate, mEnc)
           android.util.Log.e("T--", Arrays.toString(mKey))
           android.util.Log.e("T--", Arrays.toString(msgElement.toBytes))
           assert(Arrays.equals(mKey, mPlain))
       }

       def test2() {
           val (masterPublic, masterPrivate) = setup(32)
           val ids = Seq("angelo", "de caro", "unisa").map(something(masterPublic,_))
           val sk0 = keyGen(masterPublic, masterPrivate, ids(0))
           val sk01 = keyGen(masterPublic, masterPrivate, ids(0), ids(1))
           val sk012 = keyGen(masterPublic, masterPrivate, ids(0), ids(1), ids(2))
           val sk1 = keyGen(masterPublic, masterPrivate, ids(1))
           val sk10 = keyGen(masterPublic, masterPrivate, ids(1), ids(0))
           val sk021 = keyGen(masterPublic, masterPrivate, ids(0), ids(2), ids(1))

           val (key0, ciphertext0) = encaps(masterPublic, ids(0))
           val (key01, ciphertext01) = encaps(masterPublic, ids(0), ids(1))
           val (key012, ciphertext012) = encaps(masterPublic, ids(0), ids(1), ids(2))

           assert(Arrays.equals(key0, decaps(sk0, ciphertext0)))
           assert(Arrays.equals(key01, decaps(sk01, ciphertext01)))
           assert(Arrays.equals(key012, decaps(sk012, ciphertext012)))
           assert(!Arrays.equals(key0, decaps(sk1, ciphertext0)))
           assert(!Arrays.equals(key01, decaps(sk10, ciphertext01)))
           assert(!Arrays.equals(key012, decaps(sk021, ciphertext012)))
           assert(Arrays.equals(key01, decaps(delegate(masterPublic, sk0, ids(1)), ciphertext01)))
           assert(Arrays.equals(key012, decaps(delegate(masterPublic, sk01, ids(2)), ciphertext012)))
           assert(Arrays.equals(key012, decaps(delegate(masterPublic, delegate(masterPublic, sk0, ids(1)),
               ids(2)), ciphertext012)))
           assert(!Arrays.equals(key01, decaps(delegate(masterPublic, sk0, ids(0)), ciphertext01)))
           assert(!Arrays.equals(key012, decaps(delegate(masterPublic, sk01, ids(1)), ciphertext012)))
           assert(!Arrays.equals(key012, decaps(delegate(masterPublic, delegate(masterPublic, sk0, ids(2)),
               ids(1)), ciphertext012)))
       }

       def setup(bitLength: Int): (UHIBELW11PublicKeyParameters, UHIBELW11MasterSecretKeyParameters) = {
           val setup = new UHIBELW11KeyPairGenerator()
           setup.init(new UHIBELW11KeyPairGenerationParameters(bitLength))
           val pair = setup.generateKeyPair()
           (pair.getPublic.asInstanceOf[UHIBELW11PublicKeyParameters], pair.getPrivate.asInstanceOf[UHIBELW11MasterSecretKeyParameters])
       }

       def something(publicKey: UHIBELW11PublicKeyParameters, id: String): Element = {
           val pairing = PairingFactory.getPairing(publicKey.getParameters)
           val idBytes = id.getBytes
           pairing.getZr.newElementFromHash(idBytes, 0, idBytes.length)
       }

       def keyGen(masterPublic: UHIBELW11PublicKeyParameters, masterPrivate: UHIBELW11MasterSecretKeyParameters, ids: Element*): UHIBELW11SecretKeyParameters = {
           val generator = new UHIBELW11SecretKeyGenerator()
           generator.init(new UHIBELW11SecretKeyGenerationParameters(masterPrivate, masterPublic, ids.toArray))
           generator.generateKey().asInstanceOf[UHIBELW11SecretKeyParameters]
       }

       def delegate(masterPublic: UHIBELW11PublicKeyParameters, privateKey: UHIBELW11SecretKeyParameters, id: Element): UHIBELW11SecretKeyParameters = {
           val generator = new UHIBELW11SecretKeyGenerator()
           generator.init(new UHIBELW11DelegateGenerationParameters(masterPublic, privateKey, id))
           generator.generateKey().asInstanceOf[UHIBELW11SecretKeyParameters]
       }

        def encrypt(s: String, masterPublic: UHIBELW11PublicKeyParameters, ids: Element*): Array[Byte] = {
            val engine = new UHIBELW11PredicateOnlyEngine()
            engine.init(true, new ParametersWithRandom(new UHIBELW11EncryptionParameters(masterPublic, ids.toArray)))
            engine.initialize()
            val sBytes = s.getBytes
//            sBytes(0) = 5.asInstanceOf[Byte]
            engine.process(sBytes, 0, sBytes.length)
        }
        def decrypt(cipher: Array[Byte], privateKey: UHIBELW11SecretKeyParameters): Array[Byte] = {
            val engine = new UHIBELW11PredicateOnlyEngine()
            engine.init(false, new ParametersWithRandom(privateKey))
            engine.initialize()
            engine.process(cipher, 0, cipher.length)
        }


       def encaps(masterPublic: UHIBELW11PublicKeyParameters, ids: Element*): (Array[Byte], Array[Byte]) = {
           val kem = new UHIBELW11KEMEngine()
           kem.init(true, new UHIBELW11EncryptionParameters(masterPublic.asInstanceOf[UHIBELW11PublicKeyParameters],
               ids.toArray))
           val ciphertext = kem.processBlock(Array.ofDim[Byte](0), 0, 0)
           assert(ciphertext != null)
           assert(0 != ciphertext.length)
           val key = Arrays.copyOfRange(ciphertext, 0, kem.getKeyBlockSize)
           val ct = Arrays.copyOfRange(ciphertext, kem.getKeyBlockSize, ciphertext.length)
           (key, ct)
       }

       def decaps(secretKey: UHIBELW11SecretKeyParameters, cipherText: Array[Byte]): Array[Byte] = {
           val kem = new UHIBELW11KEMEngine()
           kem.init(false, secretKey)
           val key = kem.processBlock(cipherText, 0, cipherText.length)
           assert(key != null)
           assert(0 != key.length)
           key
       }
  }

class TestActivity extends Activity {
    override def onCreate(b: Bundle): Unit = {
        super.onCreate(b)
        android.util.Log.e("T--", "starting tests")
        KeyGen.test()
        android.util.Log.e("T--", "tests successful")

    }

}
