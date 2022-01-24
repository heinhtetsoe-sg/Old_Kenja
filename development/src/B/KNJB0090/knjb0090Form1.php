<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjb0090Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();
        $objForm = new form();

        //年度学期表示
        $arg["SEMESTERNAME"] = "現在年度・学期：" .CTRL_YEAR ."年度" .CTRL_SEMESTERNAME;

        //FromToの年度・学期コンボ
        $result = $db->query(knjb0090Query::getYearSemester($model));
        $opt_f_year = $opt_t_year = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //From
            if ($row["VALUE"] <= $model->ctrl_year_semester) {
                $opt_f_year[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            //To
            } else {
                $opt_t_year[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
                if (!isset($model->to_year_semester)) {
                    $model->to_year_semester = $row["VALUE"];
                }//To初期値
            }
        }
        if (!isset($model->from_year_semester)) {
            $model->from_year_semester = $model->ctrl_year_semester;
        }//From初期値

        $objForm->ae(array("type"       => "select",
                            "name"       => "FROM_YEAR_SEMESTER",
                            "size"       => "1",
                            "value"      => $model->from_year_semester,
                            "extrahtml"  => "onChange=\"return btn_submit('f_year');\"",
                            "options"    => $opt_f_year));
        $objForm->ae(array("type"       => "select",
                            "name"       => "TO_YEAR_SEMESTER",
                            "size"       => "1",
                            "value"      => $model->to_year_semester,
                            "extrahtml"  => "onChange=\"return btn_submit('t_year');\"",
                            "options"    => $opt_t_year));

        $arg["FROM_YEAR_SEMESTER"] = $objForm->ge("FROM_YEAR_SEMESTER");
        $arg["TO_YEAR_SEMESTER"] = $objForm->ge("TO_YEAR_SEMESTER");

        //Fromの基本時間割一覧リスト
        $result = $db->query(knjb0090Query::getTitle($model->from_year_semester));
        $opt_f_seq = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_f_seq[] = array("label" => $row["BSCSEQ"] ."：" .$row["TITLE"],
                                 "value" => $row["BSCSEQ"]);
        }
        if (!isset($model->from_seq) || ($model->cmd == "f_year")) {
            $model->from_seq = $opt_f_seq[0]["value"];
        }//初期値

        $objForm->ae(array("type"       => "select",
                            "name"       => "FROM_SEQ",
                            "size"       => "5",
                            "value"      => $model->from_seq,
                            "extrahtml"  => "style=\"width:220px\"",
                            "options"    => $opt_f_seq));

        $arg["FROM_SEQ"] = $objForm->ge("FROM_SEQ");

        //Toの基本時間割一覧リスト
        $result = $db->query(knjb0090Query::getTitle($model->to_year_semester));
        $opt_t_seq = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_t_seq[] = array("label" => $row["BSCSEQ"] ."：" .$row["TITLE"],
                                 "value" => $row["BSCSEQ"]);
        }

        $objForm->ae(array("type"       => "select",
                            "name"       => "TO_LIST",
                            "size"       => "5",
                            "value"      => "",
                            "extrahtml"  => "style=\"width:220px; background-color:silver\"",
                            "options"    => $opt_t_seq));

        $arg["TO_LIST"] = $objForm->ge("TO_LIST");

        //Toのタイトルテキストボックス
        $objForm->ae(array("type"        => "text",
                            "name"        => "TO_TITLE",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "value"       => $model->to_title ));

        $arg["TO_TITLE"] = $objForm->ge("TO_TITLE");

        //時間割担当チェックボックス
        if ($model->stf_chk == "on") {
            $check_header = "checked";
        } else {
            if ($model->cmd == "") {
                $check_header = "checked";
            } else {
                $check_header = "";
            }
        }
        $extra = " id=\"STF_CHK\"";
        $objForm->ae(array("type"       => "checkbox",
                            "name"      => "STF_CHK",
                            "value"     => "on",
                            "extrahtml" => $check_header.$extra ));

        $arg["STF_CHK"] = $objForm->ge("STF_CHK");

        //曜日配列
        $daycdArray = array('(日)','(月)','(火)','(水)','(木)','(金)','(土)');

        //反映履歴
        $result = $db->query(knjb0090Query::getReflecthist($model));
        $no_cnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (++$no_cnt > 100) {
                break;
            }//最大表示件数
            $row["NO"] = $no_cnt;//No.
            $row["REFLECTDATE"] = substr(str_replace("-", "/", $row["REFLECTDATE"]), 0, -7);//実施日時
            $row["B_TITLE"] = $row["BSCSEQ"] ."：" .$row["TITLE"];//適用パターン
            //反映区分
            if ($row["REFLECTDIV"] == "0") {
                $row["REFLECTDIV"] = "指定期間全て";
            }
            if ($row["REFLECTDIV"] == "1") {
                $row["REFLECTDIV"] = "１週間おき";
            }
            if ($row["REFLECTDIV"] == "2") {
                $row["REFLECTDIV"] = "２週間おき";
            }
            if ($row["REFLECTDIV"] == "3") {
                $daycd = (int) $row["DAYCD"] - 1;
                $row["REFLECTDIV"] = "曜日指定" .$daycdArray[$daycd];
                //適用期間 ... ２つの日付までを表示。３つ以上の場合、チップヘルプで表示。
                //  REFLECTDIV='3'の場合、
                //  DAYS...複数の日付のカンマ区切り。
                //  ・'12/31(金),' ... 一つの日付の場合=11バイト
                if (strlen($row["DAYS"]) <= 22) {
                    $row["SEDATE"] = $row["DAYS"];
                    $row["dayshelp"] = "";
                } else {
                    $row["SEDATE"] = substr($row["DAYS"], 0, 22) ."…";
                    $row["dayshelp"] = "onMouseOver=\"daysMousein(".$no_cnt.")\" onMouseOut=\"daysMouseout()\"";
                    $objForm->ae(array("type"      => "hidden",
                                        "name"      => "DAYS".$no_cnt,
                                        "value"     => $row["DAYS"]));
                }
            } else {
                //適用期間
                $row["SEDATE"] = str_replace("-", "/", $row["SDATE"]) ."～" .str_replace("-", "/", $row["EDATE"]);
                $row["dayshelp"] = "";
            }

            $arg["data2"][] = $row;
        }

        //コピー時のＳＥＱを取得（To年度のMAX+1）
        $seq_max = $db->getOne(knjb0090Query::getSeqMax($model));
        //echo $seq_max;
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TO_SEQ",
                            "value"     => $seq_max));

        Query::dbCheckIn($db);

        //実行ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "コピー",
                            "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb0090index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjb0090Form1.html", $arg);
    }
}
