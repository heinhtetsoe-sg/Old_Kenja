<?php

require_once('for_php7.php');

/********************************************************************/
/* 生徒環境データＣＳＶ処理                         山城 2005/10/11 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : 自転車取込を追加                         山城 2006/01/27 */
/* NO002 : 家族情報取込で生年月日NULLに対応         山城 2006/04/26 */
/* NO003 : SCHREG_ENVIR_DATの変更に伴う修正。       山城 2006/06/20 */
/********************************************************************/
//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knjh140Form1
{
    public function main($model)
    {
        $objForm = new form();

        $db  = Query::dbCheckOut();

        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //対象データコンボ
        $opt_target[] = array("label" => "1：家族情報",            "value" => 1);
        $opt_target[] = array("label" => "2：緊急連絡先情報",       "value" => 2);
        $opt_target[] = array("label" => "3：通学手段情報",         "value" => 3);
        $opt_target[] = array("label" => "4：自転車許可番号登録",    "value" => 4); //NO001

        $objForm->ae(array("type"        => "select",
                            "name"        => "TARGET",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('change_target')\";",
                            "value"       => $model->target,
                            "options"     => $opt_target));
        $arg["data"]["TARGET"] = $objForm->ge("TARGET");

        //対象ファイル
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
                                    "size"      => 2048000,
                                    "extrahtml" => "" ));
        $arg["data"]["FILE"] = $objForm->ge("FILE");

        //ヘッダ有無
        $objForm->ae(array("type"        => "checkbox",
                            "name"        => "HEADERCHECK",
                            "value"       => "1",
                            "extrahtml"   => (($model->headercheck == "1")? "checked" : "")." id=\"HEADERCHECK\"" ));
        $arg["data"]["HEADERCHECK"] = $objForm->ge("HEADERCHECK");

        //年度一覧コンボボックス
        $db         = Query::dbCheckOut();
        $optnull    = array("label" => "(全て出力)","value" => "");   //初期値：空白項目
        $result     = $db->query(knjh140query::getSelectFieldSQL($model));
        $opt_year  = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"],
                                    "value" => $row["YEAR"].$row["SEMESTER"]);
        }
        if ($model->field["YEAR"]=="") {
            $model->field["YEAR"] = CTRL_YEAR.CTRL_SEMESTER;
        }

        //年組一覧コンボボックス
        $result     = $db->query(knjh140query::getSelectFieldSQL2($model));
        $opt_gr_hr  = array();
        $opt_gr_hr[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_gr_hr[] = array("label" => $row["HR_NAME"],
                                    "value" => $row["GRADE"].$row["HR_CLASS"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度一覧コンボボックス
        $objForm->ae(array("type"        => "select",
                            "name"        => "YEAR",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"btn_submit('');\"",
                            "value"       => $model->field["YEAR"],
                            "options"     => $opt_year ));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //年組一覧コンボボックス
        $objForm->ae(array("type"        => "select",
                            "name"        => "GRADE_HR_CLASS",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->field["GRADE_HR_CLASS"],
                            "options"     => $opt_gr_hr ));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "実  行",
                            "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終  了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_output",
                            "value"     => "テンプレート書出し",
                            "extrahtml" => "onclick=\"return btn_submit('output');\"" ));

        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_csv",
                            "value"     => "データ出力",
                            "extrahtml" => "onclick=\"return btn_submit('csv');\"" ));

        $arg["button"] = array("BTN_OK"     => $objForm->ge("btn_ok"),
                               "BTN_CLEAR"  => $objForm->ge("btn_cancel"),
                               "BTN_CSV"    => $objForm->ge("btn_csv"),
                               "BTN_OUTPUT" => $objForm->ge("btn_output"));
        //hidden
        $objForm->ae(array("type"  => "hidden",
                            "name"  => "cmd"));

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh140index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjh140Form1.html", $arg);
    }
}
