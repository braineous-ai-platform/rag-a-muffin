import express from 'express';
import type {Request, Response} from 'express';

// bootstrap_application
const app = express();
const port = process.env.PORT || 3000;

// '/' endpoint
app.get("/", (request: Request, response: Response) => {
    response.send(process.env.OPENAI_API_KEY);
});

// start_the_server
app.listen(port, () => {
    console.log(`Server is running : ${port}`);
});