from flask import Flask, request, jsonify, send_from_directory
from dotenv import load_dotenv
from langchain_core.messages import HumanMessage
from backend.agent_tools import agent_executor

load_dotenv()

app = Flask(__name__, static_folder="frontend", static_url_path="")

@app.route("/")
def index():
    return send_from_directory("frontend", "index.html")

@app.route("/chat", methods=["POST"])
def chat():
    data = request.json
    user_input = data.get("message", "")
    result = agent_executor.invoke({"messages": [HumanMessage(content=user_input)]})

    # Extract final AI response from the last message (typically an AIMessage)
    output = ""
    if isinstance(result, dict) and "messages" in result:
        messages = result["messages"]
        if messages and hasattr(messages[-1], "content"):
            output = messages[-1].content

    return jsonify({"response": output or "No response generated."})

if __name__ == "__main__":
    app.run(debug=True)




