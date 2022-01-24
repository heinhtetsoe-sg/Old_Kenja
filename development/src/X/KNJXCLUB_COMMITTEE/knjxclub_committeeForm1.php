<?php

require_once('for_php7.php');

class knjxclub_committeeForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //画面サイズ設定（初期値）
        $main_width = "100%";

        //参照テーブル切替
        //部活動
        if ($model->hyoujiFlg === '1') {
            $arg["CLUB_HYOUJI"] = "1";
            $query = knjxclub_committeeQuery::getClub($model, $model->schregno, $model->year);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $arg["data"][] = array("CLUBNAME"       => $row["CLUBNAME"],
                                       "DETAIL_DATE"    => $row["DETAIL_DATE"],
                                       "DETAIL_REMARK"  => $row["DETAIL_REMARK"]);
            }
            $result->free();

        //資格
        } else if ($model->hyoujiFlg === '2') {
            $arg["AWARD_HYOUJI"] = "1";

            //画面サイズ設定
            $main_width = "800";

            //項目名
            $array = array();
            if ($model->Properties["useSchregQualified"] == 'SUBCLASS_QUALIFIED_TEST_DAT') {
                $array[1] = array("資格",       "width=\"55%\"");
                $array[2] = array("テスト",     "width=\"25%\"");
                $array[3] = array("試験日",     "width=\"20%\"");
            } else if ($model->Properties["useQualifiedMst"] == '1') {
                $array[1] = array("名称 ／ 級・段位",   "");
                $array[2] = array("取得日",             "");
                $array[3] = array("備考",               "");
            } else {
                $array[1] = array("内容",       "");
                $array[2] = array("取得日",     "");
                $array[3] = array("備考",       "");
            }
            foreach ($array as $key => $val) {
                list($title, $option) = $val;
                $arg["TITLE".$key]          = $title;
                $arg["TITLE_OPTION".$key]   = $option;
            }

            //データ表示
            $query = knjxclub_committeeQuery::getAward($model, $model->schregno, $model->year);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->Properties["useSchregQualified"] == 'SUBCLASS_QUALIFIED_TEST_DAT') {
                    $arg["data"][] = array("DATA1"          => $row["QUALIFIED_SHOW"],
                                           "DATA2"          => $row["TEST_SHOW"],
                                           "DATA3"          => str_replace("-", "/", $row["TEST_DATE"]),
                                           "DATA_OPTION1"   => "",
                                           "DATA_OPTION2"   => "",
                                           "DATA_OPTION3"   => "align=\"center\""
                                           );
                } else {
                    $arg["data"][] = array("DATA1"          => $row["HYOUJI_CONTENTS"],
                                           "DATA2"          => str_replace("-", "/", $row["REGDDATE"]),
                                           "DATA3"          => $row["REMARK"],
                                           "DATA_OPTION1"   => "",
                                           "DATA_OPTION2"   => "",
                                           "DATA_OPTION3"   => ""
                                           );
                }
            }
            $result->free();

        //委員会
        } else {
            $arg["COMMITTEE_HYOUJI"] = "1";
            $query = knjxclub_committeeQuery::getCommittee($model, $model->schregno, $model->year);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $arg["data"][] = array("COMMITTEENAME_CHARGENAME" => $row["SEQ"] . ":" . $row["COMMITTEENAME"] . " ／ " . $row["CHARGENAME"],
                                       "DETAIL_DATE"    => $row["DETAIL_DATE"],
                                       "DETAIL_REMARK"  => $row["DETAIL_REMARK"]);
            }
            $result->free();
        }

        //画面サイズ
        $arg["MAIN_WIDTH"] = $main_width;

        //年度
        $arg["YEAR"] = $model->year;

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxclub_committeeQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["NAME"] = $schName;

        //終了ボタンを作成する
        if ($model->buttonFlg) {
            $extra = "onclick=\"closeWin()\"";
        } else {
            $extra = "onclick=\"return parent.closeit()\"";
        }
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXCLUB_COMMITTEE");
        knjCreateHidden($objForm, "cmd", "");

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxclub_committeeForm1.html", $arg);
    }
}
?>
