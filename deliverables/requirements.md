# User Requirements
## Functional Requirements
- High: The user shall be able to create a unique identity.
- High: The user shall be able to send plain text messages to all others who follow the user.
- High: The user shall be able to ‘follow’ any user and receive messages sent by that user. 
- High: The user shall be able to send messages without requiring an Internet connection.
- Low: The user shall be able to send messages via the Internet to Internet-connected nodes.

## Non-Functional Requirements
- High: The network shall be usable on a large number of mobile devices.
- Medium: The user shall be able to be confident in the origin and integrity of a message.
- Low: The user shall be able to use the network with minimal training.

# System Requirements
## Functional Requirements
- High: The system shall work on portable electronic devices such as smartphones or tablets.
- High: The system shall allow creation of user identities with a unique cryptographic identity.
- High: The system shall automatically connect to nearby nodes and pass on relevant information.
- High: The system shall pass on messages until they reach their destination.
- Medium: The system shall ensure that messages cannot be modified in transit or that any such modifications can be detected.
- Medium: The system shall ensure that nodes cannot send a message that appears to be from another user.
- Medium: The system shall restrict the size of the message store.
- Medium: The system shall protect against Sybil attacks.
- Medium: The system shall prevent messages from being modified while in transit.
- Medium: The system shall protect user metadata from all other nodes.
- Medium: The system shall be scalable to an arbitrary number of nodes.
- Low: The system shall block attempts to prevent message propagation.
- Low: The system shall have mechanisms to mitigate Denial of Service attacks.

## Non-Functional Requirements
- High: The system shall work in an unstructured environment with random encounters between nodes.
- High: The system shall not require a connection to any other network (such as the Internet).
- Medium: The system shall be robust and able to continue functioning when it encounters an unexpected state such as a malfunctioning or untrustworthy node.
- Medium: The system shall minimise the number of messages lost before they reach their destination.
- Medium: The system shall route messages effectively given a semi-predictable set of encounters between nodes.
- Medium: The system shall deliver messages as quickly as possible.
