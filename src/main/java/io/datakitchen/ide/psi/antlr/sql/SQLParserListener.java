// Generated from /home/carlos/laburo/dk/dkide/src/grammars/sql/SQLParser.g4 by ANTLR 4.9.1

package io.datakitchen.ide.psi.antlr.sql;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SQLParser}.
 */
public interface SQLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SQLParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(SQLParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(SQLParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt_list(SQLParser.Sql_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt_list(SQLParser.Sql_stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt(SQLParser.Sql_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt(SQLParser.Sql_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_stmt(SQLParser.Alter_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_stmt(SQLParser.Alter_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_stmt(SQLParser.Analyze_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_stmt(SQLParser.Analyze_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#attach_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAttach_stmt(SQLParser.Attach_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#attach_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAttach_stmt(SQLParser.Attach_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBegin_stmt(SQLParser.Begin_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBegin_stmt(SQLParser.Begin_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommit_stmt(SQLParser.Commit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommit_stmt(SQLParser.Commit_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_stmt(SQLParser.Rollback_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_stmt(SQLParser.Rollback_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_stmt(SQLParser.Savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_stmt(SQLParser.Savepoint_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRelease_stmt(SQLParser.Release_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRelease_stmt(SQLParser.Release_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_stmt(SQLParser.Create_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_stmt(SQLParser.Create_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#indexed_column}.
	 * @param ctx the parse tree
	 */
	void enterIndexed_column(SQLParser.Indexed_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#indexed_column}.
	 * @param ctx the parse tree
	 */
	void exitIndexed_column(SQLParser.Indexed_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_stmt(SQLParser.Create_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_def}.
	 * @param ctx the parse tree
	 */
	void enterColumn_def(SQLParser.Column_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_def}.
	 * @param ctx the parse tree
	 */
	void exitColumn_def(SQLParser.Column_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(SQLParser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(SQLParser.Type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void enterColumn_constraint(SQLParser.Column_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void exitColumn_constraint(SQLParser.Column_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#signed_number}.
	 * @param ctx the parse tree
	 */
	void enterSigned_number(SQLParser.Signed_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#signed_number}.
	 * @param ctx the parse tree
	 */
	void exitSigned_number(SQLParser.Signed_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void enterTable_constraint(SQLParser.Table_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void exitTable_constraint(SQLParser.Table_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#foreign_key_clause}.
	 * @param ctx the parse tree
	 */
	void enterForeign_key_clause(SQLParser.Foreign_key_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#foreign_key_clause}.
	 * @param ctx the parse tree
	 */
	void exitForeign_key_clause(SQLParser.Foreign_key_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#conflict_clause}.
	 * @param ctx the parse tree
	 */
	void enterConflict_clause(SQLParser.Conflict_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#conflict_clause}.
	 * @param ctx the parse tree
	 */
	void exitConflict_clause(SQLParser.Conflict_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trigger_stmt(SQLParser.Create_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trigger_stmt(SQLParser.Create_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_stmt(SQLParser.Create_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_stmt(SQLParser.Create_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_virtual_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_virtual_table_stmt(SQLParser.Create_virtual_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_virtual_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_virtual_table_stmt(SQLParser.Create_virtual_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void enterWith_clause(SQLParser.With_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void exitWith_clause(SQLParser.With_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#cte_table_name}.
	 * @param ctx the parse tree
	 */
	void enterCte_table_name(SQLParser.Cte_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#cte_table_name}.
	 * @param ctx the parse tree
	 */
	void exitCte_table_name(SQLParser.Cte_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#recursive_cte}.
	 * @param ctx the parse tree
	 */
	void enterRecursive_cte(SQLParser.Recursive_cteContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#recursive_cte}.
	 * @param ctx the parse tree
	 */
	void exitRecursive_cte(SQLParser.Recursive_cteContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_expression(SQLParser.Common_table_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_expression(SQLParser.Common_table_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt(SQLParser.Delete_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt(SQLParser.Delete_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#delete_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt_limited(SQLParser.Delete_stmt_limitedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#delete_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt_limited(SQLParser.Delete_stmt_limitedContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#detach_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDetach_stmt(SQLParser.Detach_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#detach_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDetach_stmt(SQLParser.Detach_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_stmt(SQLParser.Drop_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_stmt(SQLParser.Drop_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(SQLParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(SQLParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#raise_function}.
	 * @param ctx the parse tree
	 */
	void enterRaise_function(SQLParser.Raise_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#raise_function}.
	 * @param ctx the parse tree
	 */
	void exitRaise_function(SQLParser.Raise_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void enterLiteral_value(SQLParser.Literal_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void exitLiteral_value(SQLParser.Literal_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInsert_stmt(SQLParser.Insert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInsert_stmt(SQLParser.Insert_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#upsert_clause}.
	 * @param ctx the parse tree
	 */
	void enterUpsert_clause(SQLParser.Upsert_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#upsert_clause}.
	 * @param ctx the parse tree
	 */
	void exitUpsert_clause(SQLParser.Upsert_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#pragma_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPragma_stmt(SQLParser.Pragma_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#pragma_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPragma_stmt(SQLParser.Pragma_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#pragma_value}.
	 * @param ctx the parse tree
	 */
	void enterPragma_value(SQLParser.Pragma_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#pragma_value}.
	 * @param ctx the parse tree
	 */
	void exitPragma_value(SQLParser.Pragma_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReindex_stmt(SQLParser.Reindex_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReindex_stmt(SQLParser.Reindex_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_stmt(SQLParser.Select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_stmt(SQLParser.Select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void enterJoin_clause(SQLParser.Join_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void exitJoin_clause(SQLParser.Join_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#select_core}.
	 * @param ctx the parse tree
	 */
	void enterSelect_core(SQLParser.Select_coreContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#select_core}.
	 * @param ctx the parse tree
	 */
	void exitSelect_core(SQLParser.Select_coreContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#factored_select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFactored_select_stmt(SQLParser.Factored_select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#factored_select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFactored_select_stmt(SQLParser.Factored_select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#simple_select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSimple_select_stmt(SQLParser.Simple_select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#simple_select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSimple_select_stmt(SQLParser.Simple_select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#compound_select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCompound_select_stmt(SQLParser.Compound_select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#compound_select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCompound_select_stmt(SQLParser.Compound_select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_or_subquery}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_subquery(SQLParser.Table_or_subqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_or_subquery}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_subquery(SQLParser.Table_or_subqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#result_column}.
	 * @param ctx the parse tree
	 */
	void enterResult_column(SQLParser.Result_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#result_column}.
	 * @param ctx the parse tree
	 */
	void exitResult_column(SQLParser.Result_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#join_operator}.
	 * @param ctx the parse tree
	 */
	void enterJoin_operator(SQLParser.Join_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#join_operator}.
	 * @param ctx the parse tree
	 */
	void exitJoin_operator(SQLParser.Join_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#join_constraint}.
	 * @param ctx the parse tree
	 */
	void enterJoin_constraint(SQLParser.Join_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#join_constraint}.
	 * @param ctx the parse tree
	 */
	void exitJoin_constraint(SQLParser.Join_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#compound_operator}.
	 * @param ctx the parse tree
	 */
	void enterCompound_operator(SQLParser.Compound_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#compound_operator}.
	 * @param ctx the parse tree
	 */
	void exitCompound_operator(SQLParser.Compound_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt(SQLParser.Update_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt(SQLParser.Update_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_list(SQLParser.Column_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_list(SQLParser.Column_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#update_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt_limited(SQLParser.Update_stmt_limitedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#update_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt_limited(SQLParser.Update_stmt_limitedContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#qualified_table_name}.
	 * @param ctx the parse tree
	 */
	void enterQualified_table_name(SQLParser.Qualified_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#qualified_table_name}.
	 * @param ctx the parse tree
	 */
	void exitQualified_table_name(SQLParser.Qualified_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void enterVacuum_stmt(SQLParser.Vacuum_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void exitVacuum_stmt(SQLParser.Vacuum_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#filter_clause}.
	 * @param ctx the parse tree
	 */
	void enterFilter_clause(SQLParser.Filter_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#filter_clause}.
	 * @param ctx the parse tree
	 */
	void exitFilter_clause(SQLParser.Filter_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#window_defn}.
	 * @param ctx the parse tree
	 */
	void enterWindow_defn(SQLParser.Window_defnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#window_defn}.
	 * @param ctx the parse tree
	 */
	void exitWindow_defn(SQLParser.Window_defnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#over_clause}.
	 * @param ctx the parse tree
	 */
	void enterOver_clause(SQLParser.Over_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#over_clause}.
	 * @param ctx the parse tree
	 */
	void exitOver_clause(SQLParser.Over_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#frame_spec}.
	 * @param ctx the parse tree
	 */
	void enterFrame_spec(SQLParser.Frame_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#frame_spec}.
	 * @param ctx the parse tree
	 */
	void exitFrame_spec(SQLParser.Frame_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#frame_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrame_clause(SQLParser.Frame_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#frame_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrame_clause(SQLParser.Frame_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#simple_function_invocation}.
	 * @param ctx the parse tree
	 */
	void enterSimple_function_invocation(SQLParser.Simple_function_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#simple_function_invocation}.
	 * @param ctx the parse tree
	 */
	void exitSimple_function_invocation(SQLParser.Simple_function_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#aggregate_function_invocation}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_function_invocation(SQLParser.Aggregate_function_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#aggregate_function_invocation}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_function_invocation(SQLParser.Aggregate_function_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#window_function_invocation}.
	 * @param ctx the parse tree
	 */
	void enterWindow_function_invocation(SQLParser.Window_function_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#window_function_invocation}.
	 * @param ctx the parse tree
	 */
	void exitWindow_function_invocation(SQLParser.Window_function_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#common_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_stmt(SQLParser.Common_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#common_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_stmt(SQLParser.Common_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#order_by_stmt}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_stmt(SQLParser.Order_by_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#order_by_stmt}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_stmt(SQLParser.Order_by_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#limit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterLimit_stmt(SQLParser.Limit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#limit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitLimit_stmt(SQLParser.Limit_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#ordering_term}.
	 * @param ctx the parse tree
	 */
	void enterOrdering_term(SQLParser.Ordering_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#ordering_term}.
	 * @param ctx the parse tree
	 */
	void exitOrdering_term(SQLParser.Ordering_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void enterAsc_desc(SQLParser.Asc_descContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void exitAsc_desc(SQLParser.Asc_descContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#frame_left}.
	 * @param ctx the parse tree
	 */
	void enterFrame_left(SQLParser.Frame_leftContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#frame_left}.
	 * @param ctx the parse tree
	 */
	void exitFrame_left(SQLParser.Frame_leftContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#frame_right}.
	 * @param ctx the parse tree
	 */
	void enterFrame_right(SQLParser.Frame_rightContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#frame_right}.
	 * @param ctx the parse tree
	 */
	void exitFrame_right(SQLParser.Frame_rightContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#frame_single}.
	 * @param ctx the parse tree
	 */
	void enterFrame_single(SQLParser.Frame_singleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#frame_single}.
	 * @param ctx the parse tree
	 */
	void exitFrame_single(SQLParser.Frame_singleContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#window_function}.
	 * @param ctx the parse tree
	 */
	void enterWindow_function(SQLParser.Window_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#window_function}.
	 * @param ctx the parse tree
	 */
	void exitWindow_function(SQLParser.Window_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#of_OF_fset}.
	 * @param ctx the parse tree
	 */
	void enterOf_OF_fset(SQLParser.Of_OF_fsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#of_OF_fset}.
	 * @param ctx the parse tree
	 */
	void exitOf_OF_fset(SQLParser.Of_OF_fsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#default_DEFAULT__value}.
	 * @param ctx the parse tree
	 */
	void enterDefault_DEFAULT__value(SQLParser.Default_DEFAULT__valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#default_DEFAULT__value}.
	 * @param ctx the parse tree
	 */
	void exitDefault_DEFAULT__value(SQLParser.Default_DEFAULT__valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#partition_by}.
	 * @param ctx the parse tree
	 */
	void enterPartition_by(SQLParser.Partition_byContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#partition_by}.
	 * @param ctx the parse tree
	 */
	void exitPartition_by(SQLParser.Partition_byContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#order_by_expr}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_expr(SQLParser.Order_by_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#order_by_expr}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_expr(SQLParser.Order_by_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#order_by_expr_asc_desc}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_expr_asc_desc(SQLParser.Order_by_expr_asc_descContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#order_by_expr_asc_desc}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_expr_asc_desc(SQLParser.Order_by_expr_asc_descContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#expr_asc_desc}.
	 * @param ctx the parse tree
	 */
	void enterExpr_asc_desc(SQLParser.Expr_asc_descContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#expr_asc_desc}.
	 * @param ctx the parse tree
	 */
	void exitExpr_asc_desc(SQLParser.Expr_asc_descContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#initial_select}.
	 * @param ctx the parse tree
	 */
	void enterInitial_select(SQLParser.Initial_selectContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#initial_select}.
	 * @param ctx the parse tree
	 */
	void exitInitial_select(SQLParser.Initial_selectContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#recursive__select}.
	 * @param ctx the parse tree
	 */
	void enterRecursive__select(SQLParser.Recursive__selectContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#recursive__select}.
	 * @param ctx the parse tree
	 */
	void exitRecursive__select(SQLParser.Recursive__selectContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#unary_operator}.
	 * @param ctx the parse tree
	 */
	void enterUnary_operator(SQLParser.Unary_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#unary_operator}.
	 * @param ctx the parse tree
	 */
	void exitUnary_operator(SQLParser.Unary_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#error_message}.
	 * @param ctx the parse tree
	 */
	void enterError_message(SQLParser.Error_messageContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#error_message}.
	 * @param ctx the parse tree
	 */
	void exitError_message(SQLParser.Error_messageContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#module_argument}.
	 * @param ctx the parse tree
	 */
	void enterModule_argument(SQLParser.Module_argumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#module_argument}.
	 * @param ctx the parse tree
	 */
	void exitModule_argument(SQLParser.Module_argumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(SQLParser.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(SQLParser.Column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#keyword}.
	 * @param ctx the parse tree
	 */
	void enterKeyword(SQLParser.KeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#keyword}.
	 * @param ctx the parse tree
	 */
	void exitKeyword(SQLParser.KeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(SQLParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(SQLParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(SQLParser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(SQLParser.Function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(SQLParser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(SQLParser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_or_index_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_index_name(SQLParser.Table_or_index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_or_index_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_index_name(SQLParser.Table_or_index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#new_table_name}.
	 * @param ctx the parse tree
	 */
	void enterNew_table_name(SQLParser.New_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#new_table_name}.
	 * @param ctx the parse tree
	 */
	void exitNew_table_name(SQLParser.New_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void enterCollation_name(SQLParser.Collation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void exitCollation_name(SQLParser.Collation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#foreign_table}.
	 * @param ctx the parse tree
	 */
	void enterForeign_table(SQLParser.Foreign_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#foreign_table}.
	 * @param ctx the parse tree
	 */
	void exitForeign_table(SQLParser.Foreign_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(SQLParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(SQLParser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_name(SQLParser.Trigger_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_name(SQLParser.Trigger_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(SQLParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(SQLParser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#module_name}.
	 * @param ctx the parse tree
	 */
	void enterModule_name(SQLParser.Module_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#module_name}.
	 * @param ctx the parse tree
	 */
	void exitModule_name(SQLParser.Module_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#pragma_name}.
	 * @param ctx the parse tree
	 */
	void enterPragma_name(SQLParser.Pragma_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#pragma_name}.
	 * @param ctx the parse tree
	 */
	void exitPragma_name(SQLParser.Pragma_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_name(SQLParser.Savepoint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_name(SQLParser.Savepoint_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias(SQLParser.Table_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias(SQLParser.Table_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#transaction_name}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_name(SQLParser.Transaction_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#transaction_name}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_name(SQLParser.Transaction_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#window_name}.
	 * @param ctx the parse tree
	 */
	void enterWindow_name(SQLParser.Window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#window_name}.
	 * @param ctx the parse tree
	 */
	void exitWindow_name(SQLParser.Window_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(SQLParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(SQLParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#filename}.
	 * @param ctx the parse tree
	 */
	void enterFilename(SQLParser.FilenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#filename}.
	 * @param ctx the parse tree
	 */
	void exitFilename(SQLParser.FilenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#base_window_name}.
	 * @param ctx the parse tree
	 */
	void enterBase_window_name(SQLParser.Base_window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#base_window_name}.
	 * @param ctx the parse tree
	 */
	void exitBase_window_name(SQLParser.Base_window_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#simple_func}.
	 * @param ctx the parse tree
	 */
	void enterSimple_func(SQLParser.Simple_funcContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#simple_func}.
	 * @param ctx the parse tree
	 */
	void exitSimple_func(SQLParser.Simple_funcContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#aggregate_func}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_func(SQLParser.Aggregate_funcContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#aggregate_func}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_func(SQLParser.Aggregate_funcContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_function_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_function_name(SQLParser.Table_function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_function_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_function_name(SQLParser.Table_function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#any_name}.
	 * @param ctx the parse tree
	 */
	void enterAny_name(SQLParser.Any_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#any_name}.
	 * @param ctx the parse tree
	 */
	void exitAny_name(SQLParser.Any_nameContext ctx);
}