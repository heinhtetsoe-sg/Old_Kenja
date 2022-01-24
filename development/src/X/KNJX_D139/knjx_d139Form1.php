<?php

require_once('for_php7.php');

class knjx_d139Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //DB接続
        $db = Query::dbCheckOut();
        
        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", $size);

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt_shubetsu = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 4096000);

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種を表示
            $arg["usePrgSchoolkind"] = 1; 
            //校種コンボ作成
            $query = knjx_d139Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('')\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "", $model);
        }else{
            $model->field["SCHOOL_KIND"] = "P";
        }

        //学期コンボ作成
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        }else{
            $setNameCd = "ZP09";
        }
        $query = knjx_d139Query::getNameMst("",$setNameCd,"");
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "", $model);

        //年組コンボ作成
        $query = knjx_d139Query::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1, $model);

        //教科コンボ作成
        $query = knjx_d139Query::getClassMst($model, "", $model->field["GRADE_HR_CLASS"]);
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank", $model);

        //科目コンボ作成
        $query = knjx_d139Query::getSubclassMst($model->field["CLASSCD"], $model->field["GRADE_HR_CLASS"], $model);
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank", $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJX_D139");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx_d139index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_d139Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", &$model) {
    $opt = array();
    $value_flg = false;
    $dataFlg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        $dataFlg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        if ($model->Properties["use_prg_schoolkind"] != "1") {
            if(!$dataFlg){
                //名称マスタ「ZP09」が取得できなかった場合、名称マスタ「Z009」を参照
                $result = $db->query(knjd139Query::getNameMst("Z009"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                    if ($value == $row["VALUE"]) $value_flg = true;
                }
                $result->free();
            }
        }
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
