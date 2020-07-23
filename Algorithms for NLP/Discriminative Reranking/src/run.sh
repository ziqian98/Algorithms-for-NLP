ant -f build_assign_rerank.xml
export DATA_PATH="/Users/luoziqian/IdeaProjects/711HW3/rerank-data"
java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx6000m edu.berkeley.nlp.assignments.rerank.ParsingRerankerTester -path $DATA_PATH -rerankerType AWESOME -test