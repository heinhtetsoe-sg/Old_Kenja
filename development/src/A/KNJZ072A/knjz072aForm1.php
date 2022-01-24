<?php

require_once('for_php7.php');

class knjz072aForm1
{
    function main($model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz072aindex.php", "", "sel");
        $db = Query::dbCheckOut();
        
        //年度設定
        $query = knjz072aQuery::selectYearQuery();
        $result = $db->query($query);
        $opt = array();
        $j=0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"], 
                           "value" => $row["YEAR"]);
            if ($model->year==$row["YEAR"]) $j++;
        }
        if ($j==0) {
            $model->year = $opt[0]["value"];
        }
        
        //年度科目一覧取得
        $query = knjz072aQuery::selectQuery($model);
        $result = $db->query($query);
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        
        $opt_right = array();
        //科目一覧取得
        $query = knjz072aQuery::selectSubclassQuery($opt_left_id,$model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt));

        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "" )); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));
        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        //年度科目
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_left)); 

        //科目マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassmaster",
                            "size"        => "20",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_right));  

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"moves('left');\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"move1('left');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"move1('right');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"moves('right');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("subclassyear"),
                                   "RIGHT_PART"  => $objForm->ge("subclassmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //科目マスタボタン
        $link = REQUESTROOT."/Z/KNJZ072_2A/knjz072_2aindex.php?year_code=".$model->year;
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "BTN_MASTER", " 科目マスタ ", $extra);
        //教科科目名称マスタのマスタ画面のボタン表示設定
        //詳細登録ボタン(仮のプロパティKNJZ070_3用
        if ($model->Properties["hyoujiClassDetailDat"] == 1) {
            $arg["hyoujiClassDetailDat"] = '1';
        }
        $link = REQUESTROOT."/Z/KNJZ070_3/knjz070_3index.php?mode=1&SEND_YEAR=$model->year&SEND_PRGID=KNJZ072A&SEND_AUTH=".AUTHORITY."";
        //詳細登録
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_SHOUSAI"] = knjCreateBtn($objForm, "BTN_SHOUSAI", "詳細登録", $extra);
        //保存ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "更新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "BTN_CLEAR", "取消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "終了", $extra);
        //教育委員会科目取込ボタン
        $link = REQUESTROOT."/Z/KNJZ072_SUBCLASS_REFLECTION/knjz072_subclass_reflectionindex.php?SEND_AUTH=".AUTHORITY." ";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_BOARD"] = knjCreateBtn($objForm, "BTN_BOARD", "教育委員会科目取込", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "temp_year");

        $arg["info"] = array("TOP"        => "対象年度",
                             "LEFT_LIST"  => "年度科目一覧",
                             "RIGHT_LIST" => "科目一覧");
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "マスタメンテナンスー科目マスタ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz072aForm1.html", $arg); 
    }
}
?>
