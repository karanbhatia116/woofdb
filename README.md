# WoofDB

* WoofDB is an embedded, almost SQL based (not quite yet) database inspired by SQLite and DuckDB.
* It provides REPL interface to connect and interact with the database (Soon to move to expose over TCP)

## Pending items:

- [X] Implement SQL tokenizer
- [X] Implement SQL parser
- [X] Remove hardcoded users table
- [ ] Move table persistence to a Page based technique to store and retrieve data instead of loading everything in memory
- [ ] Implement B-Trees for table persistence
- [ ] Implement logic for indexes

Quite to figure out...


