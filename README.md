# WebRunner

WebRunner is a desktop tool for manual and semi-automated API testing.
Its core feature is the ability to execute JavaScript before and after the HTTP request, 
manage request collections, perform variable substitution, and work with a syntax-highlighted JSON editor.

### Features

- Requests and Collections tree
- Before and After request JS executor
- Vars Definition - use `{{variableName}}` in body and define `vars` variable in Before block, 
and it will be replaced in request body
- Log everything! - You getting and can write all the logs you need
- Global Variables
- A lot of context - Before request and After request contains all the needed context for you: 
payloads, headers, vars and much more
- Hotkeys

and much more!

## Killer Feature

Chaining. Chaining allows you to call request by request, having all the context about previous requests.

You just create chain, select all needed request in needed order, and it will execute all of it

Inside every Before and After block of all you will be able to access chain context 
(all the meta information about chain, chain's before block and requests)'
and Global Variables

--- 
**For instance**: You need to create some entity, and then check it has been created, then compare all the needed fields.

Just create Chain! Register request for creating entity, then register request to find created entity and in block 'After' of chain just compare all of it!

> Remember! You have all the needed context

---

### API protocols

- http
- grpc

### Tech stack

- Java 21
- JavaFX
- RichTextFX
- GraalVM Polyglot JavaScript
- Jackson
- Gradle