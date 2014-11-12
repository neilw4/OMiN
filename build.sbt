import android.Keys._

android.Plugin.androidBuild

name := "OMiN"

scalacOptions += "-feature"

run <<= run in Android

install <<= install in Android

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick" % "2.0.0"
)

proguardOptions in Android ++= Seq(
  "-dontwarn javax.naming.InitialContext",
  "-dontwarn org.bouncycastle.x509.util.LDAPStoreHelper",
  "-dontwarn org.bouncycastle.jce.provider.X509LDAPCertStoreSpi",
  "-dontwarn com.sun.jna.Native",
  "-dontwarn org.bouncycastle.util.AllTests",
  "-dontwarn org.bouncycastle.util.io.pem.AllTests",
  "-dontnote org.slf4j.**",
  "-keep class scala.collection.Seq.**",
  "-keep public class org.sqldroid.**",
  "-keep class scala.concurrent.Future$.**",
  "-keep class scala.slick.driver.JdbcProfile$Implicits",
  "-keep class com.sun.jna.Native$ffi_callback",
  "-keep class com.sun.jna.FromNativeConverter",
  "-keep class com.sun.jna.ToNativeConverter",
  "-keep class com.sun.jna.ToNativeConverter",
  "-keep class com.sun.jna.Structure",
  "-keep class com.sun.jna.Callback",
  "-keep class it.unisa.dia.gas.crypto.jpbc.fe.ibe.lw11.engines.UHIBELW11PredicateOnlyEngine"
)

proguardCache in Android ++= Seq(
  ProguardCache("slick") % "com.typesafe.slick"
)
