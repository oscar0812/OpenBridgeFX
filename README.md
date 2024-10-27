Here’s the updated README with the new instructions included:

# OpenBridgeFX

OpenBridgeFX is a JavaFX application that allows users to interact with a conversational AI system. Users can load past conversations, send prompts with dynamic parameters, and view chat-style responses.

## Features

- Load previous conversations from a dropdown.
- Input prompts with placeholders and provide key-value pairs for dynamic replacement.
- Display chat messages in a conversational format, including time-stamped responses.
- Automatically saves conversation history to JSON files.

## Getting Started

1. Clone the repository.
2. Open the project in your preferred Java IDE.
3. Locate the .jar file under `out/artifacts/OpenBridgeFx_jar`.
4. Create an `.env` file in the same directory with the following values:
    - `API_KEY=<your_api_key>`
    - `API_URL=https://api.openai.com/v1/chat/completions`
5. Run the application: ```java -jar <jarfile.jar>```

## How to Use

- **Loading a conversation**: Select a saved conversation from the dropdown to view the previous chat.
- **Sending a prompt**: Type your message in the text area. If your prompt includes placeholders (e.g., {name}), fields will appear to enter the corresponding values.
- **Viewing responses**: The responses from the AI will appear in chat bubbles below the input area.

## FXML Layout

The user interface is split into two main sections:
- A text area for user input and parameter fields.
- A scrollable chat window for displaying the conversation history.

## License

This project is licensed under the MIT License.