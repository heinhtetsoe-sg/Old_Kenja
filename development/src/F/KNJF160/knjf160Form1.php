<?php

require_once('for_php7.php');

class knjf160Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf160index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒項目名
        $sch_label = $sep = "";
        for ($i = 0; $i < mb_strlen($model->sch_label); $i++) {
            $sch_label .= $sep.mb_substr($model->sch_label, $i, 1);
            $sep = "<br>";
        }
        $arg["SCH_LABEL"] = $sch_label;

        //データを取得
        $row = $db->getRow(knjf160Query::selectQuery($model), DB_FETCHMODE_ASSOC);

        $arg["data"]["SCHREGNO"]            = $row["SCHREGNO"];
        $arg["data"]["SCH_INFO"]            = ($row["ATTENDNO"]) ? $row["HR_NAME"].$row["ATTENDNO"].'番' : $row["HR_NAME"];
        $arg["data"]["NAME"]                = $row["NAME"];
        $arg["data"]["NAME_KANA"]           = $row["NAME_KANA"];
        $arg["data"]["BIRTHDAY"]            = ($row["BIRTHDAY"]) ? common::DateConv1(str_replace("-", "/", $row["BIRTHDAY"]),0).'生' : "";
        $arg["data"]["ZIPCD"]               = '〒'.$row["ZIPCD"];
        $arg["data"]["ADDR"]                = $row["ADDR1"]."<br>".$row["ADDR2"];
        $arg["data"]["ADDR2"]               = $row["ADDR2"];
        $arg["data"]["TELNO"]               = $row["TELNO"];
        $arg["data"]["GUARD_NAME"]          = $row["GUARD_NAME"];
        $arg["data"]["GUARD_KANA"]          = $row["GUARD_KANA"];
        $arg["data"]["RELATIONSHIP"]        = ($row["RELATIONSHIP"]) ? $db->getOne(knjf160Query::getNameMst('H201', $row["RELATIONSHIP"])) : "";
        if($row["SCHREGNO"] && $row["GUARD_ADDR1"] && ($row["ZIPCD"] == $row["GUARD_ZIPCD"]) && ($row["ADDR1"] == $row["GUARD_ADDR1"]) && ($row["ADDR2"] == $row["GUARD_ADDR2"])){
            $arg["data"]["GUARD_ZIPCD"]         = '〒';
            $arg["data"]["GUARD_ADDR"]         = $model->sch_label."の欄に同じ";
        } else {
            $arg["data"]["GUARD_ZIPCD"]        = '〒'.$row["GUARD_ZIPCD"];
            $arg["data"]["GUARD_ADDR"]         = $row["GUARD_ADDR1"]."<br>".$row["GUARD_ADDR2"];
        }
        $arg["data"]["GUARD_WORK_NAME"]     = $row["GUARD_WORK_NAME"];
        $arg["data"]["GUARD_TELNO"]         = $row["GUARD_TELNO"];
        $arg["data"]["EMERGENCYNAME"]       = $row["EMERGENCYNAME"];
        $arg["data"]["EMERGENCYRELA_NAME"]  = $row["EMERGENCYRELA_NAME"];
        $arg["data"]["EMERGENCYCALL"]       = $row["EMERGENCYCALL"];
        $arg["data"]["EMERGENCYTELNO"]      = $row["EMERGENCYTELNO"];
        $arg["data"]["EMERGENCYNAME2"]      = $row["EMERGENCYNAME2"];
        $arg["data"]["EMERGENCYRELA_NAME2"] = $row["EMERGENCYRELA_NAME2"];
        $arg["data"]["EMERGENCYCALL2"]      = $row["EMERGENCYCALL2"];
        $arg["data"]["EMERGENCYTELNO2"]     = $row["EMERGENCYTELNO2"];

        $clubname = $db->getCol(knjf160Query::getClubName($model));
        foreach ($clubname as $key => $val) $arg["data"]["CLUB".((int)$key+1)] = $val;

        //スポーツ振興センター
        $cntB = $db->getOne(knjf160Query::getCntBaseRemark1($model));
        $cntA = $db->getOne(knjf160Query::getCntAnotherSport($model));
        if ($cntB > 0 && $cntA > 0) {
            $arg["data"]["SPORT_LABEL"] = "加入済み（前籍校）";
        } else if ($cntB > 0 && $cntA == 0) {
            $arg["data"]["SPORT_LABEL"] = "加入済み";
        } else {
            $arg["data"]["SPORT_LABEL"] = "未加入";
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $model->seq = "";

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf160Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //病気けが(入学前)ボタン
    $extra = "style=\"height:30px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "病気けが(入学前)", $extra);
    //病気けが(入学後)履歴ボタン
    $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "病気けが(入学後)", $extra);
    //家族情報ボタン
    $extra = "style=\"height:30px;background:#FFFF00;color:#FF8C00;font:bold\" onclick=\"return btn_submit('subform3');\"";
    $arg["button"]["btn_subform3"] = KnjCreateBtn($objForm, "btn_subform3", "家族情報", $extra);
    //その他調査ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "その他調査", $extra);
    //健康調査ボタン
    $extra = "style=\"height:30px;background:#C0FFFF;color:#1E90FF;font:bold\" onclick=\"return btn_submit('subform5');\"";
    $arg["button"]["btn_subform5"] = KnjCreateBtn($objForm, "btn_subform5", "健康調査", $extra);
    //保健室記入ボタン
    $extra = "style=\"height:30px;background:#FFDEAD;color:#D2691E;font:bold\" onclick=\"return btn_submit('subform6');\"";
    $arg["button"]["btn_subform6"] = KnjCreateBtn($objForm, "btn_subform6", "保健室記入", $extra);
    //終了ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
?>
