<?php

require_once('for_php7.php');


class knja170oForm1
{
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja170oForm1", "POST", "knja170oindex.php", "", "knja170oForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $opt=array();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ) );

        if (is_numeric($model->control["学期数"])){
            //年度,学期コンボの設定
            for ( $i = 0; $i < (int)$model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $model->control["学期名"][$i+1], 
                              "value" => sprintf("%d", $i+1)
                             );            
            }
        }
        if (!isset($model->field["OUTPUT"])){
            $model->field["OUTPUT"] = $model->control["学期"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "OUTPUT",
                            "size"       => "1",
                            "value"      => $model->field["OUTPUT"],
                            "extrahtml"  => "onChange=\"return btn_submit('knja170o');\"",
                            "options"    => $opt));

        $arg["data"]["OUTPUT"] = $objForm->ge("OUTPUT");


        if(isset($model->field["OUTPUT"])) {
            $ga = $model->field["OUTPUT"];
        }
        else {
            $ga = $model->control["学期"];
        }

        //クラス選択コンボボックスを作成する
        $query = knja170oQuery::getAuth($ga, $model);

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => " onChange=\"return btn_submit('read');\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //クラス選択コンボボックスを作成する
        $query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
                    "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
                    "WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ".    // 02/10/04 nakamoto
                    "((SCHREG_REGD_DAT.SEMESTER)='" .$ga ."') AND ".
                    "((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))".   //  04/07/22  yamauchi
                    "ORDER BY ATTENDNO";
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' =>  $row["NAME"],
                            'value' => $row["SCHREGNO"]);
        }
        $result->free();

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象取り消しボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "　＞　",
                            "extrahtml"   => " onclick=\"move('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取り消しボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right2",
                            "value"       => "　≫　",
                            "extrahtml"   => " onclick=\"move('rightall');\"" ) );

        $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

        //対象選択ボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "　＜　",
                            "extrahtml"   => " onclick=\"move('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //対象選択ボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left2",
                            "value"       => "　≪　",
                            "extrahtml"   => " onclick=\"move('leftall');\"" ) );

        $arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

        //電話番号チェックボックス作成
        if ($model->field["TEL"] == "1" || $model->cmd == ""){
            $check_tel = "checked";
        }else {
            $check_tel = "";
        }

        $objForm->ae( array("type"  => "checkbox",
                            "name"  => "TEL",
                            "value" => "1",
                            "extrahtml" => $check_tel ) );

        $arg["data"]["TEL"] = $objForm->ge("TEL");

        //印刷ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_print",
                            "value"     => "プレビュー／印刷",
                            "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "', '', '', '');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knja170oQuery::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "', 'csv');\"";
            $setBtnName = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
            $setBtnName = "ＣＳＶ出力";
        }
        //セキュリティーチェック
        $securityCnt = $db->getOne(knja170oQuery::getSecurityHigh());
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $setBtnName, $extra);
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SDAY", str_replace("/","-",$model->control["学期開始日付"][$model->field["OUTPUT"]]));
        knjCreateHidden($objForm, "EDAY", str_replace("/","-",$model->control["学期終了日付"][$model->field["OUTPUT"]]));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA170O");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //CSV用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja170oForm1.html", $arg); 
    }
}
?>
