from langchain_core.messages import HumanMessage
from langchain_openai import ChatOpenAI
from langchain.tools import tool
from langgraph.prebuilt import create_react_agent

from sympy import symbols, Eq, solve
import re

from datetime import datetime

from itertools import permutations

from dotenv import load_dotenv

load_dotenv()

from langchain.tools import tool

@tool
def calculator(a: float, b: float, operation: str) -> str:
    """Tool for performing arithmetic operations between two numbers.
    Supported operations: add, subtract, multiply, divide, power
    """
    print("The tool has been called")
    if operation == "add":
        return f"The sum of {a} and {b} is {a + b}"
    elif operation == "subtract":
        return f"The difference between {a} and {b} is {a - b}"
    elif operation == "multiply":
        return f"The product of {a} and {b} is {a * b}"
    elif operation == "divide":
        if b == 0:
            return "Division by zero is not allowed."
        return f"{a} divided by {b} is {a / b}"
    elif operation == "power":
        return f"{a} raised to the power of {b} is {a ** b}"
    else:
        return "Invalid operation. Please use add, subtract, multiply, divide, or power."


@tool 
def solve_equation(equation: str) -> str:
    """Solves simple algebra equations like 'x + 7 = 14'"""
    try:
        x = symbols('x')
        # Convert the string into an equation
        left, right = equation.split('=')
        eq = Eq(eval(left.strip()), eval(right.strip()))
        solution = solve(eq, x)
        return f"The solution to the equation is x = {solution[0]}"
    except Exception as e:
        return f"Could not solve the equation. Error: {str(e)}"
    
@tool
def convert_units(value: float, from_unit: str, to_unit: str) -> str:
    """Converts value from one unit to another (e.g., cm to inches)."""
    conversions = {
        ("cm", "inch"): lambda v: v / 2.54,
        ("inch", "cm"): lambda v: v * 2.54,
        ("kg", "lb"): lambda v: v * 2.20462,
        ("lb", "kg"): lambda v: v / 2.20462,
        ("c", "f"): lambda v: (v * 9/5) + 32,
        ("f", "c"): lambda v: (v - 32) * 5/9
    }
    key = (from_unit.lower(), to_unit.lower())
    if key in conversions:
        result = conversions[key](value)
        return f"{value} {from_unit} is {result:.2f} {to_unit}"
    return "Unsupported unit conversion."

@tool
def current_time() -> str:
    """Returns the current date and time."""
    return datetime.now().strftime("It's %A, %B %d, %Y at %I:%M %p.")

@tool
def solve_word_problem(problem: str) -> str:
    """Solves basic word problems involving arithmetic or linear equations."""
    x = symbols('x')

    # Handle a few common patterns manually
    # You can expand this with NLP techniques later

    if "doubled" in problem and "increased by" in problem and "result is" in problem:
        match = re.search(r"doubled.*?(\d+).*?result is (\d+)", problem)
        if match:
            add = int(match.group(1))
            result = int(match.group(2))
            eq = Eq(2*x + add, result)
            sol = solve(eq, x)
            return f"Solving 2x + {add} = {result}, we get x = {sol[0]}"
    
    if "tripled" in problem and "decreased by" in problem and "equals" in problem:
        match = re.search(r"tripled.*?(\d+).*?equals (\d+)", problem)
        if match:
            sub = int(match.group(1))
            result = int(match.group(2))
            eq = Eq(3*x - sub, result)
            sol = solve(eq, x)
            return f"Solving 3x - {sub} = {result}, we get x = {sol[0]}"

    return "Sorry, I couldn't understand that problem. Try rephrasing or use a more common format."

@tool
def logic_grid_solver(problem: str) -> str:
    """
    Solves a basic logic grid puzzle with 3 people and 3 things using predefined constraints.
    Only works for a specific format (can be expanded later).
    """
    people = ["Alice", "Bob", "Carol"]
    drinks = ["tea", "coffee", "juice"]

    # All possible assignments of drinks to people
    for perm in permutations(drinks):
        assignments = dict(zip(people, perm))

        # Apply constraints
        if assignments["Alice"] == "coffee":
            continue
        if assignments["Bob"] == "tea":
            continue

        # Found a valid assignment
        return "\n".join([f"{person} likes {drink}" for person, drink in assignments.items()])

    return "No solution found based on the given constraints."


# Export for app.py
tools = [calculator, solve_equation, convert_units, current_time, solve_word_problem, logic_grid_solver]
model = ChatOpenAI(temperature=0)
agent_executor = create_react_agent(model, tools)


def main():
    model = ChatOpenAI(temperature=0)
    
    tools = [calculator, solve_equation, convert_units, current_time, solve_word_problem, logic_grid_solver]
    agent_executor = create_react_agent(model, tools)
    
    print("Welcome! I'm your AI assistant. Type 'quit' to exit.")
    print("You can ask me to perform calculations or chat with me.")
    
    while True:
        user_input = input("\nYou: ").strip()
        
        if user_input == "quit":
            break
        
        print("\nAssistant: ", end="")
        for chunk in agent_executor.stream(
            {"messages": [HumanMessage(content=user_input)]}
        ):
            if "agent" in chunk and "messages" in chunk["agent"]:
                for message in chunk["agent"]["messages"]:
                    print(message.content, end="")
        print()
        
if __name__ == "__main__":
    main()