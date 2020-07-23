ant -f build_assign_align.xml
DATA_PATH="/Users/luoziqian/IdeaProjects/711HW4/align_data"
java -cp assign_align.jar:assign_align-submit.jar -server -Xmx8g edu.berkeley.nlp.assignments.align.AlignmentTester -path $DATA_PATH -alignerType HMM -maxTrain 10000 -data test