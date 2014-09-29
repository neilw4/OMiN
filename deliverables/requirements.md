# User Requirements
## Non-Functional Requirements
- High: The user shall be able to create a profile.
- High: The user shall be able to broadcast text messages to all others who follow the user or a hashtag in the message.
- High: The user shall be able to 'follow' any user and recieve messages sent by that user.
- Low: The user shall be able to 'follow' any hashtag and recieve messages containing that hashtag.
- Low: The user shall be able to send encrypted direct messages to a single user.
- Low: The user shall be able to broadcast multimedia messages to all others who follow the user or a hashtag in the message.

# Functional Requirements
# System Requirements
## Non-Functional Requirements
- High: The system shall work on smartphones or tablets capable of connecting to a wifi network.
- High: The system shall allow creation of user profiles with a unique user ID and cryptographic identity.
- High: The system shall automatically connect to nearby nodes and pass on relevant information such as messages.
- Medium: The system shall assume that all network nodes cannot be trusted.
- Medium: The system shall provide a mechanism for securely distributing the cryptographic identity of a user.
- Medium: The system shall protect user metadata such as location and friends list from all other nodes.
- Medium: The system shall ensure that messages cannot be modified in transit or that such modifications can be detected.
- Medium: The system shall ensure that nodes cannot send a message that appears to be from another user.
- Medium: The system shall be robust and able to continue functioning when it encounters an unexpected state.
- Medium: The system shall ensure that encrypted direct messages cannot be read by third parties.
- Low: The system shall still be able to function if a node has no associated user profile.
- Low: The system shall be able to support multiple user profiles on a single node.
- Low: The system shall send anonymous logging data to a central server for debugging and profiling purposes.

# Functional Requirements
- Medium: The system shall ensure that messages sent by a user will reach at least 50% of intented recipients.
- Medium: The system shall evict messages from the message buffer if the buffer size exceeds some maximum size.
