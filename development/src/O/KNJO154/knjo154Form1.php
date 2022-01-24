<?php
require_once('for_php7.php');
class knjo154Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjo154index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //対象生徒のSCHOOLKINDを取得したい
        if($model->schregno != ""){
            $query = knjo154Query::getSchoolKind($model->schregno);
            $schoolKind = $db->getOne($query);
        }else{
            $schoolKind = SCHOOLKIND;
        }
        if($schoolKind != "H"){
            $arg["PRIMARY_JUNIOR"] = 1;
        }else{
            $arg["HIGHSCHOOL"] = 1;
        }
        
        //生徒基本情報
        $query = knjo154Query::getStudent($model->schregno);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if(!empty($row)){
            $arg["SCHREGNO"] = $row["SCHREGNO"];
            $arg["NAME"] = $row["NAME"];
            $birthday = explode("-", $row["BIRTHDAY"]);
            $arg["YEAR"] = $birthday[0];
            $arg["MONTH"] = $birthday[1];
            $arg["DAY"] = $birthday[2];
        }
        
        //ラジオボタン作成
        $query = knjo154Query::getYear($model->schregno, $schoolKind);
        $result = $db->query($query);
        $cnt = 0;
        $opt = array();
        $label = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[$cnt+1] = $row["YEAR"]; //value
            $idcnt = $cnt+1;
            $idname[$cnt] = "YEAR{$idcnt}";
            $label[$cnt] = $row["YEAR"]."年度";
            
            $cnt++;
        }
        $option = " onclick=\" btn_submit('year_change');\"";
        $extraCnt = get_count($opt);
        $extraRadio = array();
        for($i=0;$i<$extraCnt;$i++){
            $extraRadio[$i] = "id=\"{$idname[$i]}\" {$option}";
        }
        $radioArray = knjCreateRadio($objForm, "YEAR", $model->field["YEAR"], $extraRadio, $opt, get_count($opt));
        $arg["YEARRADIO"] = "";
        $j=0;
        foreach($radioArray as $key => $val){
            $arg["YEARRADIO"] .= $val."<LABEL for=\"".$idname[$j]."\">".$label[$j]."</LABEL>　　";
            $j++;
        }
        
        if($model->field["YEAR"] != ""){
            //変換用配列作成
            //視力
            $query = knjo154Query::getCode("02", $schoolKind);
            $result = $db->query($query);
            $vision = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $vision[$row["DICTIONARY_CODE"]] = $row["CODENAME"];
            }
            //聴力
            $query = knjo154Query::getCode("03", $schoolKind);
            $result = $db->query($query);
            $hear = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $hear[$row["DICTIONARY_CODE"]] = $row["CODENAME"];
            }
            //異常の有無
            $query = knjo154Query::getCode("04", $schoolKind);
            $result = $db->query($query);
            $disorder = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $disorder[$row["DICTIONARY_CODE"]] = $row["CODENAME"];
            }
            //指導区分
            $query = knjo154Query::getCode("05", $schoolKind);
            $result = $db->query($query);
            $shidou = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $shidou[$row["DICTIONARY_CODE"]] = $row["CODENAME"];
            }
            //尿検査
            $query = knjo154Query::getCode("06", $schoolKind);
            $result = $db->query($query);
            $urine = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $urine[$row["DICTIONARY_CODE"]] = $row["CODENAME"];
            }

            //詳細画面作成
            $query = knjo154Query::getMedicalData($model->schregno, $opt[$model->field["YEAR"]], $schoolKind);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            //視力
            $row["RIGHT_RAGAN"] = $vision[$row["N_EYESIGHT_RIGHT"]];
            $row["LEFT_RAGAN"] = $vision[$row["N_EYESIGHT_LEFT"]];
            $row["RIGHT_KYOUSEI"] = $vision[$row["C_EYESIGHT_RIGHT"]];
            $row["LEFT_KYOUSEI"] = $vision[$row["C_EYESIGHT_LEFT"]];
            
            //聴力
            $row["RIGHT_EAR_RESULT"] = $hear[$row["HEAR_RESULT_RIGHT"]];
            $row["LEFT_EAR_RESULT"] = $hear[$row["HEAR_RESULT_LEFT"]];
            $row["RIGHT_EAR_LEVEL"] = $row["HEAR_LEVEL_RIGHT"];
            $row["LEFT_EAR_LEVEL"] = $row["HEAR_LEVEL_LEFT"];
            
            //栄養状態
            $row["NUT_DISORDER"] = $disorder[$row["NUT_DISORDER"]];
            if($row["NUT_DISEASE_GNAME"] != ""){
                $row["NUT_DISEASE_GNAME"] = "(".$row["NUT_DISEASE_GNAME"].")";
            }
            //脊柱胸郭四肢
            //疾病及び異常
            $row["SCL_DISORDER"] = $disorder[$row["SCL_DISORDER"]];
            if($row["SCL_DISEASE_GNAME"] != ""){
                $row["SCL_DISEASE_GNAME"] = "(".$row["SCL_DISEASE_GNAME"].")";
            }
            //脊柱
            $row["SPINE_DISORDER"] = $disorder[$row["SPINE_DISORDER"]];
            if($row["SPINE_DISEASE_GNAME"] != ""){
                $row["SPINE_DISEASE_GNAME"] = "(".$row["SPINE_DISEASE_GNAME"].")";
            }
            //胸郭
            $row["CHEST_DISORDER"] = $disorder[$row["CHEST_DISORDER"]];
            if($row["CHEST_DISEASE_GNAME"] != ""){
                $row["CHEST_DISEASE_GNAME"] = "(".$row["CHEST_DISEASE_GNAME"].")";
            }
            //四肢
            $row["LIMB_DISORDER"] = $disorder[$row["LIMB_DISORDER"]];
            if($row["LIMB_DISEASE_GNAME"] != ""){
                $row["LIMB_DISEASE_GNAME"] = "(".$row["LIMB_DISEASE_GNAME"].")";
            }
            //眼の疾病及び異常
            $row["EYE_DISORDER"] = $disorder[$row["EYE_DISORDER"]];
            if($row["EYE_DISEASE_GNAME"] != ""){
                $row["EYE_DISEASE_GNAME"] = "(".$row["EYE_DISEASE_GNAME"].")";
            }
            //耳鼻咽頭疾患
            //疾病及び異常
            $row["ENT_DISORDER"] = $disorder[$row["ENT_DISORDER"]];
            if($row["ENT_DISEASE_GNAME"] != ""){
                $row["ENT_DISEASE_GNAME"] = "(".$row["ENT_DISEASE_GNAME"].")";
            }
            //耳
            $row["EAR_DISORDER"] = $disorder[$row["EAR_DISORDER"]];
            if($row["EAR_DISEASE_GNAME"] != ""){
                $row["EAR_DISEASE_GNAME"] = "(".$row["EAR_DISEASE_GNAME"].")";
            }
            //鼻
            $row["NOSE_DISORDER"] = $disorder[$row["NOSE_DISORDER"]];
            if($row["NOSE_DISEASE_GNAME"] != ""){
                $row["NOSE_DISEASE_GNAME"] = "(".$row["NOSE_DISEASE_GNAME"].")";
            }
            //咽頭
            $row["THROAT_DISORDER"] = $disorder[$row["THROAT_DISORDER"]];
            if($row["THROAT_DISEASE_GNAME"] != ""){
                $row["THROAT_DISEASE_GNAME"] = "(".$row["THROAT_DISEASE_GNAME"].")";
            }
            //皮膚疾患
            $row["SKIN_DISORDER"] = $disorder[$row["SKIN_DISORDER"]];
            if($row["SKIN_DISEASE_GNAME"] != ""){
                $row["SKIN_DISEASE_GNAME"] = "(".$row["SKIN_DISEASE_GNAME"].")";
            }
            //結核＞疾病及び異常
            $row["TB_DISORDER"] = $disorder[$row["TB_DISORDER"]];
            if($row["TB_DISEASE_GNAME"] != ""){
                $row["TB_DISEASE_GNAME"] = "(".$row["TB_DISEASE_GNAME"].")";
            }
            $row["TB_SHIDOU_KUBUN"] = $shidou[$row["TB_SHIDOU_KUBUN"]];
            
            //心臓
            if($row["HEART_TEST_GNAME"] != ""){
                $row["HEART_TEST_GNAME"] = "(".$row["HEART_TEST_GNAME"].")";
            }
            $row["HEART_DISORDER"] = $disorder[$row["HEART_DISORDER"]];
            if($row["HEART_DISEASE_GNAME"] != ""){
                $row["HEART_DISEASE_GNAME"] = "(".$row["HEART_DISEASE_GNAME"].")";
            }
            
            //尿
            $row["URINE_PROTEINE"] = $urine[$row["URINE_PROTEINE"]];
            $row["URINE_SUGER"] = $urine[$row["URINE_SUGER"]];
            $row["URINE_BLOOD"] = $urine[$row["URINE_BLOOD"]];

            //寄生虫卵
            $row["PARA_DISORDER"] = $disorder[$row["PARA_DISORDER"]];
            if($row["PARA_DISEASE_GNAME"] != ""){
                $row["PARA_DISEASE_GNAME"] = "(".$row["PARA_DISEASE_GNAME"].")";
            }
            
            //その他の疾病及び異常
            $row["OTHER_DISORDER"] = $disorder[$row["OTHER_DISORDER"]];
            if($row["OTHER_DISEASE_GNAME"] != ""){
                $row["OTHER_DISEASE_GNAME"] = "(".$row["OTHER_DISEASE_GNAME"].")";
            }
            
            
            //歯科検査情報のその他の疾病及び異常、学校歯科医所見、事後措置を表示
            $query = knjo154Query::getDentalData($model->schregno, $opt[$model->field["YEAR"]], $schoolKind);
            $drow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row["DENT_OTHER_DISORDER"] = $disorder[$drow["OTHER_DISORDER"]];
            $row["DENT_OTHER_DISEASE_NNAME"] = $drow["OTHER_DISEASE_NNAME"];
            if($drow["OTHER_DISEASE_GNAME"] != ""){
                $row["DENT_OTHER_DISEASE_GNAME"] = "(".$drow["OTHER_DISEASE_GNAME"].")";
            }
            $row["SCH_DENTIST_SYOKEN"] = $drow["SCH_DENTIST_SYOKEN"];
            $row["SCH_DENTIST_DATE"] = $drow["SCH_DENTIST_DATE"];
            $row["DENT_AFTERCARE"] = $drow["AFTERCARE"];
            
            
            $arg["data"] = $row;
        }
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjo154Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //表示
    $arg["button"]["btn_search"] = createBtn($objForm, "btn_search", "表 示", "");
    //CSV
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "CSV出力", "");
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("sort", $model->sort));
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
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

?>
