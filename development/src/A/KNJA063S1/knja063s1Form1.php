<?php

require_once('for_php7.php');

class knja063s1Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knja063s1index.php", "", "list");
        $db             = Query::dbCheckOut();

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度・学期
        $arg["term"] = $db->getOne(knja063s1Query::getTerm($model));

        //対象複式クラスコンボ
        $query = knja063s1Query::getGhrCd($model);
        $extra = "tabindex=\"1\" onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->ghr_cd, "GHR_CD", $extra, 1);

        //担任名
        $arg["teacher"] = $db->getOne(knja063s1Query::getTeacher($model));

        //ソート
        $model->sorttype = $db->getOne(knja063s1Query::getSort());

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

        $arg["sort1"] = View::alink("knja063s1index.php", "性別＋氏名かな".$mark1, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort1" => ($model->sort["1"] == "1")?"0":"1",
                                      "s_id"  => "1") );

        $arg["sort2"] = View::alink("knja063s1index.php", "氏名かな＋性別".$mark2, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort2" => ($model->sort["2"] == "1")?"0":"1",
                                      "s_id"  => "2") );

        $arg["sort3"] = View::alink("knja063s1index.php", "実クラス出席番号順".$mark3, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort3" => ($model->sort["3"] == "1")?"0":"1",
                                      "s_id"  => "3") );

        $arg["sort4"] = View::alink("knja063s1index.php", "年組番順".$mark4, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort4" => ($model->sort["4"] == "1")?"0":"1",
                                      "s_id"  => "4") );

        //生徒一覧
        $i  = 0; //タブインデックス用
        $ii = 2; 
        $attendno = $model->attendno;

        $model->schregno = array();
        $result = $db->query(knja063s1Query::getStudents($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $model->schregno[] = $row["SCHREGNO"];

            $row["NENKUMIBAN"] = $row["HR_NAME"] . $row["ATTENDNO"] . "番";

            $objForm->ae( array("type"        => "text",
                                "name"        => "GHR_ATTENDNO",
                                "size"        => 4,
                                "maxlength"   => 3,
                                "multiple"    => 1,
                                "extrahtml"   => "tabindex=\"$ii\" style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"",
                                "value"       => $row["GHR_ATTENDNO"] ));
            $row["GHR_ATTENDNO"]    = $objForm->ge("GHR_ATTENDNO");

            $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";
            $arg["data"][] = $row; 
            $i++;
            $ii++;
        }

        Query::dbCheckIn($db);

        $disBtn = ($i > 0) ? "" : " disabled";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "出席番号クリア",
                            "extrahtml"   => "tabindex=\"$ii\" onclick=\"return ClearAttendno();\"" . $disBtn) ); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_attend",
                            "value"       => "出席番号自動作成",
                            "extrahtml"   => "tabindex=\"$ii+1\" onclick=\"return MakeOrder();\"" . $disBtn) ); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_copy",
                            "value"       => "旧学期の出席番号をコピー",
                            "extrahtml"   => "tabindex=\"$ii+2\" onclick=\"return btn_submit('copy');\"" . " disabled") ); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "更 新",
                            "extrahtml"   => "tabindex=\"$ii+3\" onclick=\"return btn_submit('update');\"" . $disBtn) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "tabindex=\"$ii+4\" onclick=\"return btn_submit('clear');\"" . $disBtn) );
        
        $subdata  = "wopen('".REQUESTROOT."/A/KNJA063/knja063index.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID={$model->prgid}&SEND_YEAR={$model->year}&SEND_SEMESTER={$model->semester}&SEND_GHR_CD={$model->ghr_cd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "tabindex=\"$ii+5\"  onclick=\"$subdata\"" ) );

        $arg["button"] = array("BTN_DELETE"     => $objForm->ge("btn_delete"),
                               "BTN_ATTEND"     => $objForm->ge("btn_attend"),
                               "BTN_COPY"       => $objForm->ge("btn_copy"),
                               "BTN_OK"         => $objForm->ge("btn_ok"),
                               "BTN_CLEAR"      => $objForm->ge("btn_clear"),
                               "BTN_END"        => $objForm->ge("btn_end"));  
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"] = $objForm->get_finish();
        
        View::toHTML($model, "knja063s1Form1.html", $arg); 
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
