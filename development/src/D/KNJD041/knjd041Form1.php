<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：テスト項目の設定をDBからでなく、直接設定 山城 2004/10/26 */
/* ･NO002：テスト項目の設定を３学期は、期末のみ設定 山城 2004/11/29 */
/* ･NO003：出力設定ラジオボタンを追加               山城 2004/12/02 */
/********************************************************************/

class knjd041Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd041Form1", "POST", "knjd041index.php", "", "knjd041Form1");

        //DBOpen
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボの設定
        $this->makeSemesCmb($objForm, $arg, $model, $db);

        //素点・評価のラジオボタン 1:素点 2:評価
        $opt_record = array(1, 2);
        $model->field["RECORD_DIV"] = ($model->field["RECORD_DIV"] == "") ? "1" : $model->field["RECORD_DIV"];
        $extra = array("id=\"RECORD_DIV1\"", "id=\"RECORD_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "RECORD_DIV", $model->field["RECORD_DIV"], $extra, $opt_record, get_count($opt_record));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //テスト名コンボボックスを作成する
        $this->makeTestCmb($objForm, $arg, $db, $model);

        //クラス一覧リスト作成する
        $this->makeClassList($objForm, $arg, $db, $model);

        //NO003 帳票出力指定ラジオボタン
        $this->makeOutput($objForm, $arg, $model);

        //印刷ボタンを作成する
        $arg["button"]["btn_print"] = $this->createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");

        //終了ボタンを作成する
        $arg["button"]["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hiddenを作成する(必須)
        $objForm->ae($this->createHiddenAe("DBNAME", DB_DATABASE));
        $objForm->ae($this->createHiddenAe("PRGID", "KNJD041"));
        $objForm->ae($this->createHiddenAe("cmd"));
        $objForm->ae($this->createHiddenAe("YEAR", CTRL_YEAR));
        $objForm->ae($this->createHiddenAe("COUNTFLG", $model->testTable));
        $objForm->ae($this->createHiddenAe("useCurriculumcd", $model->Properties["useCurriculumcd"]));
        $objForm->ae($this->createHiddenAe("useRecordChkfinDat", $model->Properties["useRecordChkfinDat"]));

        //DBClose
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd041Form1.html", $arg); 
    }

    //学期コンボ作成
    function makeSemesCmb(&$objForm, &$arg, &$model, $db) {
        $opt = array();
        $query = knjd041Query::getSemester();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        if (!isset($model->field["GAKKI"])) {
            $model->field["GAKKI"] = CTRL_SEMESTER;
        }

        $arg["data"]["GAKKI"] = $this->createCombo($objForm, "GAKKI", $model->field["GAKKI"], $opt, "onchange=\"return btn_submit('gakki');\"", 1);
    }

    //テスト種別コンボ作成
    function makeTestCmb(&$objForm, &$arg, $db, &$model) {
        if ($model->field["GAKKI"] == "9" && $model->testTable == "TESTITEM_MST_COUNTFLG") {
            $opt = array();
            $opt[] = array('label' => '0000  評価成績', 'value' => '0');
        } else {
            $opt = array();
            $query = knjd041Query::getTestItem($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            if ($model->testTable == "TESTITEM_MST_COUNTFLG") {
                $opt[] = array('label' => '0000  評価成績', 'value' => '0');
            }
        }
        $arg["data"]["TEST"] = $this->createCombo($objForm, "TEST", $model->field["TEST"], $opt, "", 1);
    }

    //出力種別ラジオ作成
    function makeOutput(&$objForm, &$arg, $model)
    {
        $opt_sitei    = array();
        $opt_sitei[0] = 1;
        $opt_sitei[1] = 2;

        $value = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : 1;
        $this->createRadio($objForm, $arg, "OUTPUT", $value, "", $opt_sitei, get_count($opt_sitei));
    }

    //教科一覧リスト作成
    function makeClassList(&$objForm, &$arg, $db, &$model)
    {
        $row1 = array();
        $query = knjd041Query::getClassData($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["VALUE"]." ".$row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        //教科一覧リストを作成する
        $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = $this->createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

        //出力対象教科リストを作成する
        $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = $this->createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = $this->createBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = $this->createBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = $this->createBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = $this->createBtn($objForm, "btn_left1", "＜", $extra);
    }

    //コンボ作成
    function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae( array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

    //ラジオ作成
    function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
    {
        for ($i = 1; $i <= $count; $i++) {
            $id_name = $name.$i;
            $objForm->ae( array("type"      => "radio",
                                "name"      => $name,
                                "value"     => $value,
                                "extrahtml" => $extra." id=\"$id_name\"",
                                "multiple"  => $multi));
            $arg["data"][$name.$i] = $objForm->ge($name, $i);
        }
    }

    //Hidden作成ae
    function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }

}
?>
