// Generated from /home/carlos/laburo/dk/dkide/src/grammars/sql/SQLParser.g4 by ANTLR 4.9.1

package io.datakitchen.ide.psi.antlr.sql;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SQLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SQLParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(SQLParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_stmt_list(SQLParser.Sql_stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#sql_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_stmt(SQLParser.Sql_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_stmt(SQLParser.Alter_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#analyze_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze_stmt(SQLParser.Analyze_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#attach_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttach_stmt(SQLParser.Attach_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#begin_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBegin_stmt(SQLParser.Begin_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#commit_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommit_stmt(SQLParser.Commit_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#rollback_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollback_stmt(SQLParser.Rollback_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepoint_stmt(SQLParser.Savepoint_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#release_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelease_stmt(SQLParser.Release_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_index_stmt(SQLParser.Create_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#indexed_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexed_column(SQLParser.Indexed_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_def(SQLParser.Column_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_name(SQLParser.Type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_constraint(SQLParser.Column_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#signed_number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSigned_number(SQLParser.Signed_numberContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_constraint(SQLParser.Table_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#foreign_key_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeign_key_clause(SQLParser.Foreign_key_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#conflict_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConflict_clause(SQLParser.Conflict_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_trigger_stmt(SQLParser.Create_trigger_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_view_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view_stmt(SQLParser.Create_view_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_virtual_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_virtual_table_stmt(SQLParser.Create_virtual_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#with_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_clause(SQLParser.With_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#cte_table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCte_table_name(SQLParser.Cte_table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#recursive_cte}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecursive_cte(SQLParser.Recursive_cteContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#common_table_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_table_expression(SQLParser.Common_table_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#delete_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_stmt(SQLParser.Delete_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#delete_stmt_limited}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_stmt_limited(SQLParser.Delete_stmt_limitedContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#detach_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDetach_stmt(SQLParser.Detach_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#drop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_stmt(SQLParser.Drop_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(SQLParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#raise_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRaise_function(SQLParser.Raise_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#literal_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral_value(SQLParser.Literal_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#insert_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_stmt(SQLParser.Insert_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#upsert_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpsert_clause(SQLParser.Upsert_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#pragma_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_stmt(SQLParser.Pragma_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#pragma_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_value(SQLParser.Pragma_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#reindex_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindex_stmt(SQLParser.Reindex_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#select_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_stmt(SQLParser.Select_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#join_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_clause(SQLParser.Join_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#select_core}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_core(SQLParser.Select_coreContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#factored_select_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactored_select_stmt(SQLParser.Factored_select_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#simple_select_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_select_stmt(SQLParser.Simple_select_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#compound_select_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompound_select_stmt(SQLParser.Compound_select_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_or_subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_or_subquery(SQLParser.Table_or_subqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#result_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResult_column(SQLParser.Result_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#join_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_operator(SQLParser.Join_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#join_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_constraint(SQLParser.Join_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#compound_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompound_operator(SQLParser.Compound_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#update_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_stmt(SQLParser.Update_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name_list(SQLParser.Column_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#update_stmt_limited}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_stmt_limited(SQLParser.Update_stmt_limitedContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#qualified_table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualified_table_name(SQLParser.Qualified_table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVacuum_stmt(SQLParser.Vacuum_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#filter_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter_clause(SQLParser.Filter_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#window_defn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_defn(SQLParser.Window_defnContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#over_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOver_clause(SQLParser.Over_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#frame_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_spec(SQLParser.Frame_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#frame_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_clause(SQLParser.Frame_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#simple_function_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_function_invocation(SQLParser.Simple_function_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#aggregate_function_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_function_invocation(SQLParser.Aggregate_function_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#window_function_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_function_invocation(SQLParser.Window_function_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#common_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_table_stmt(SQLParser.Common_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#order_by_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_stmt(SQLParser.Order_by_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#limit_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimit_stmt(SQLParser.Limit_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#ordering_term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdering_term(SQLParser.Ordering_termContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#asc_desc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsc_desc(SQLParser.Asc_descContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#frame_left}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_left(SQLParser.Frame_leftContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#frame_right}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_right(SQLParser.Frame_rightContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#frame_single}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_single(SQLParser.Frame_singleContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#window_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_function(SQLParser.Window_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#of_OF_fset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOf_OF_fset(SQLParser.Of_OF_fsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#default_DEFAULT__value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_DEFAULT__value(SQLParser.Default_DEFAULT__valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#partition_by}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_by(SQLParser.Partition_byContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#order_by_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_expr(SQLParser.Order_by_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#order_by_expr_asc_desc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_expr_asc_desc(SQLParser.Order_by_expr_asc_descContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#expr_asc_desc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_asc_desc(SQLParser.Expr_asc_descContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#initial_select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitial_select(SQLParser.Initial_selectContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#recursive__select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecursive__select(SQLParser.Recursive__selectContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#unary_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_operator(SQLParser.Unary_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#error_message}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitError_message(SQLParser.Error_messageContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#module_argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_argument(SQLParser.Module_argumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alias(SQLParser.Column_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyword(SQLParser.KeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(SQLParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_name(SQLParser.Function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_name(SQLParser.Schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_or_index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_or_index_name(SQLParser.Table_or_index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#new_table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNew_table_name(SQLParser.New_table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#collation_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollation_name(SQLParser.Collation_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#foreign_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeign_table(SQLParser.Foreign_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_name(SQLParser.Index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#trigger_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_name(SQLParser.Trigger_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name(SQLParser.View_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#module_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_name(SQLParser.Module_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#pragma_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma_name(SQLParser.Pragma_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#savepoint_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepoint_name(SQLParser.Savepoint_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_alias(SQLParser.Table_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#transaction_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_name(SQLParser.Transaction_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#window_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_name(SQLParser.Window_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(SQLParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#filename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilename(SQLParser.FilenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#base_window_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBase_window_name(SQLParser.Base_window_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#simple_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_func(SQLParser.Simple_funcContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#aggregate_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_func(SQLParser.Aggregate_funcContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_function_name(SQLParser.Table_function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#any_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny_name(SQLParser.Any_nameContext ctx);
}