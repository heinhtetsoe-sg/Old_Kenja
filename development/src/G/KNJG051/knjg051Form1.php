<?php

require_once('for_php7.php');


class knjg051Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knjg051Form1", "POST", "knjg051index.php", "", "knjg051Form1");

		//卒業年度
		$db = Query::dbCheckOut();
		$opt_year = array();
		$query = knjg051Query::selectYear();
		$result = $db->query($query);
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_year[]= array('label' => $row["YEAR"]."年度卒",
			                   'value' => $row["YEAR"]);
		}
		$result->free();
		Query::dbCheckIn($db);

		if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;		//初期値：現在年度をセット。

		$objForm->ae( array("type"       => "select",
		                    "name"       => "YEAR",
		                    "size"       => "1",
		                    "extrahtml"  => "onChange=\"return btn_submit('knjg051');\"",
		                    "value"      => $model->field["YEAR"],
		                    "options"    => $opt_year));

		$arg["data"]["YEAR"] = $objForm->ge("YEAR");

		//学期コード・学年数上限
		$db = Query::dbCheckOut();
		$query = knjg051Query::selectGradeSemesterDiv($model);
		$row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opt_Grade    = isset($row["GRADE_HVAL"])?$row["GRADE_HVAL"]:"03";//データ無しはデフォルトで３学年を設定
        $opt_Semester = isset($row["SEMESTERDIV"])?$row["SEMESTERDIV"]:"3";//データ無しはデフォルトで３学期を設定
		Query::dbCheckIn($db);
		/* 学期コードをhiddenで送る。
		 * 卒業年度が現在年度の場合：現在学期をセット。
		 * 卒業年度が現在年度未満の場合：３学期をセット。
		 */
		if ($model->field["YEAR"] == CTRL_YEAR) {
			$model->field["GAKKI"] = CTRL_SEMESTER;
		} else {
			$model->field["GAKKI"] = $opt_Semester;
		}

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "GAKKI",
		                    "value"      => $model->field["GAKKI"]
		                    ) );
		//クラスコンボ
		$db = Query::dbCheckOut();
		$query = knjg051Query::getAuth($model,$opt_Grade);
		$result = $db->query($query);
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_class[]= array('label' => $row["LABEL"],
		                    	'value' => $row["VALUE"]);
		}
		$result->free();
		Query::dbCheckIn($db);

		if ($model->field["CMBCLASS"] == "") $model->field["CMBCLASS"] = $opt_class[0]["value"];

		$objForm->ae( array("type"       => "select",
		                    "name"       => "CMBCLASS",
		                    "size"       => "1",
		                    "extrahtml"  => "onChange=\"return btn_submit('knjg051');\"",
		                    "value"      => $model->field["CMBCLASS"],
		                    "options"    => $opt_class));

		$arg["data"]["CMBCLASS"] = $objForm->ge("CMBCLASS");

		//クラス一覧リスト作成する
		$db = Query::dbCheckOut();
		$query = knjg051Query::getstudent($model);
		$result = $db->query($query);
		while($rowst = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $rowstudent[]= array('label' => $rowst["LABEL"],
		                    	 'value' => $rowst["VALUE"]);
		}
		$result->free();
		Query::dbCheckIn($db);

		$objForm->ae( array("type"       => "select",
		                    "name"       => "STUDENT_NAME",
							"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"",
		                    "size"       => "20",
		                    "options"    => isset($rowstudent)?$rowstudent:array()));

		$arg["data"]["STUDENT_NAME"] = $objForm->ge("STUDENT_NAME");

		//出力対象クラスリストを作成する
		$objForm->ae( array("type"       => "select",
		                    "name"       => "STUDENT_SELECTED",
							"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"",
		                    "size"       => "20",
		                    "options"    => array()));

		$arg["data"]["STUDENT_SELECTED"] = $objForm->ge("STUDENT_SELECTED");

		//対象選択ボタンを作成する（全部）
		$objForm->ae( array("type" => "button",
		                    "name"        => "btn_rights",
		                    "value"       => ">>",
		                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

		$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

		//対象取消ボタンを作成する（全部）
		$objForm->ae( array("type" => "button",
		                    "name"        => "btn_lefts",
		                    "value"       => "<<",
		                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

		$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

		//対象選択ボタンを作成する（一部）
		$objForm->ae( array("type" => "button",
		                    "name"        => "btn_right1",
		                    "value"       => "＞",
		                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

		$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

		//対象取消ボタンを作成する（一部）
		$objForm->ae( array("type" => "button",
		                    "name"        => "btn_left1",
		                    "value"       => "＜",
		                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

		$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

		//記載日付
		$value = isset($model->field["NOTICEDAY"])?$model->field["NOTICEDAY"]:str_replace("-","/",$model->control["学籍処理日"]);
		$arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

		//印刷ボタンを作成する
		$objForm->ae( array("type" => "button",
		                    "name"        => "btn_print",
		                    "value"       => "プレビュー／印刷",
		                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

		$arg["button"]["btn_print"] = $objForm->ge("btn_print");

		//終了ボタンを作成する
		$objForm->ae( array("type" => "button",
		                    "name"        => "btn_end",
		                    "value"       => "終 了",
		                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

		$arg["button"]["btn_end"] = $objForm->ge("btn_end");

		//hiddenを作成する(必須)
		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "DBNAME",
		                    "value"      => DB_DATABASE
		                    ) );

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "PRGID",
		                    "value"     => "KNJG051"
		                    ) );

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "cmd"
		                    ) );

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knjg051Form1.html", $arg); 
	}
}
?>
