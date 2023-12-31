# app
Wordle Solver

**ENDPOINTS**

**==> wordlesolver/getnextword** - get best next word to guess

**inputs**:
- sessionId - identifies your session. Should be unique to each game execution.
- guess - word guessed
- result - result of word guessed; format is 5 letter string; example: bbyyg

**outputs**:
- best next word to guess

**example:**

request:

curl --location 'http://localhost:8080/wordlesolver/getnextword' \
--form 'guess="siren"' \
--form 'sessionId="abcd-9999"' \
--form 'result="bbbyy"'

response:

canoe


**==> wordlesolver/cleanup** - clean up session files for given sessionId

**inputs**:
- sessionId - sessionId of session to clean up

**outputs**:
NONE

**example:**

request:

curl --location --request DELETE 'http://localhost:8080/wordlesolver/cleanup' \
--form 'sessionId="abcd-9999"'

response:
EMPTY