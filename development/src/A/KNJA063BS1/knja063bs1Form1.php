<?php

require_once('for_php7.php');

class knja063bs1Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knja063bs1index.php", "", "list");
        $db             = Query::dbCheckOut();

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度・学期
        $arg["term"] = $db->getOne(knja063bs1Query::getTerm($model));

        //対象授業クラスコンボ
        $query = knja063bs1Query::getGhrCd($model);
        $extra = "tabindex=\"1\" onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->ghr_cd, "GHR_CD", $extra, 1);

        //担任名
        $arg["teacher"] = $db->getOne(knja063bs1Query::getTeacher($model));

        //ソート
        $model->sorttype = $db->getOne(knja063bs1Query::getSort());

        //ソート
        $mark = array("▼","▲");

        switch ($model->s_id) {
            case "1":
                $mark1 = $mark[$model->sort[$model->s_id]];break;
            case "2":
                $mark2 = $mark[$model->sort[$model->s_id]];break;
            case "3":
                $mark3 = $mark[$model->sort[$model->s_id]];break;
            case "4":
                $mark4 = $mark[$model->sort[$model->s_id]];break;
        }

        $arg["sort1"] = View::alink("knja063bs1index.php", "性別＋氏名かな".$mark1, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort1" => ($model->sort["1"] == "1")?"0":"1",
                                      "s_id"  => "1") );

        $arg["sort2"] = View::alink("knja063bs1index.php", "氏名かな＋性別".$mark2, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort2" => ($model->sort["2"] == "1")?"0":"1",
                                      "s_id"  => "2") );

        $arg["sort3"] = View::alink("knja063bs1index.php", "授業クラス出席番号順".$mark3, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort3" => ($model->sort["3"] == "1")?"0":"1",
                                      "s_id"  => "3") );

        $arg["sort4"] = View::alink("knja063bs1index.php", "年組番順".$mark4, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort4" => ($model->sort["4"] == "1")?"0":"1",
                                      "s_id"  => "4") );

        //生徒一覧
        $i  = 0; //タブインデックス用
        $ii = 2; 
        $attendno = $model->attendno;

        $model->schregno = array();
        $result = $db->query(knja063bs1Query::getStudents($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $model->schregno[] = $row["SCHREGNO"];

            $row["NENKUMIBAN"] = $row["HR_NAME"] . $row["ATTENDNO"] . "番";

            $extra = "tabindex=\"$ii\" style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $row["GHR_ATTENDNO"] = knjCreateTextBox($objForm, $row["GHR_ATTENDNO"], "GHR_ATTENDNO", 4, 3, $extra, 1);

            $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";
            $arg["data"][] = $row; 
            $i++;
            $ii++;
        }

        Query::dbCheckIn($db);

        $disBtn = ($i > 0) ? "" : " disabled";

        //button
        $extra = "tabindex=\"$ii\" onclick=\"return ClearAttendno();\"" . $disBtn;
        $arg["button"]["BTN_DELETE"] = knjCreateBtn($objForm, "btn_delete", "出席番号クリア", $extra);

        $extra = "tabindex=\"$ii+1\" onclick=\"return MakeOrder();\"" . $disBtn;
        $arg["button"]["BTN_ATTEND"] = knjCreateBtn($objForm, "btn_attend", "出席番号自動作成", $extra);

        $extra = "tabindex=\"$ii+3\" onclick=\"return btn_submit('update');\"" . $disBtn;
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "更 新", $extra);

        $extra = "tabindex=\"$ii+4\" onclick=\"return btn_submit('clear');\"" . $disBtn;
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        $subdata  = "wopen('".REQUESTROOT."/A/KNJA063B/knja063bindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID={$model->prgid}&SEND_KIRIKAE={$model->hr_kirikae}&SEND_YEAR={$model->year}&SEND_SEMESTER={$model->semester}&SEND_GHR_CD={$model->ghr_cd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "tabindex=\"$ii+5\"  onclick=\"$subdata\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knja063bs1Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
