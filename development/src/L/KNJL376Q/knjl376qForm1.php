<?php

require_once('for_php7.php');

class knjl376qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl376qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        
        $db = Query::dbCheckOut();
        
        //表示区分
        $opt = array(1, 2, 3);
        $onclick = " onclick=\"btn_submit('search');\"";
        $extra = array("id=\"Radio1\" {$onclick}", "id=\"Radio2\" {$onclick}", "id=\"Radio3\" {$onclick}");
        $label = array("Radio1" => "全データ", "Radio2" => "間違いのあるもののみ", "Radio3" => "未入力");
        $radioArray = knjCreateRadio($objForm, "Radio", $model->field["RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        //表示データ取得
        //カウントする
        $cntQuery = knjl376qQuery::getData($model->field["RADIO"], "1");
        $cnt = $db->getOne($cntQuery);
        
        if($cnt > 0){
            $dataQuery = knjl376qQuery::getData($model->field["RADIO"]);
            $dataResult = $db->query($dataQuery);
            
            $data = array();
            
            //更新するときに使う
            $model->satNo = array();
            
            while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
                //受験番号
                $data["SAT_NO"] = $dataRow["SAT_NO"];
                
                //名前
                $data["NAME"] = $dataRow["NAME1"];
                
                if($model->field["RADIO"] != "1"){  //得点はテキストボックス
                    //英語
                    $data["ENG_FIRST"]  = knjCreateTextBox($objForm, $dataRow["SCORE_ENGLISH"], "ENG_FIRST{$dataRow["SAT_NO"]}", 5, 3, $extra);
                    $data["ENG_SECOND"] = knjCreateTextBox($objForm, $dataRow["SCORE_ENGLISH2"], "ENG_SECOND{$dataRow["SAT_NO"]}", 5, 3, $extra);
                    
                    //数学
                    $data["MATH_FIRST"]  = knjCreateTextBox($objForm, $dataRow["SCORE_MATH"], "MATH_FIRST{$dataRow["SAT_NO"]}", 5, 3, $extra);
                    $data["MATH_SECOND"] = knjCreateTextBox($objForm, $dataRow["SCORE_MATH2"], "MATH_SECOND{$dataRow["SAT_NO"]}", 5, 3, $extra);

                    //国語
                    $data["JAP_FIRST"]  = knjCreateTextBox($objForm, $dataRow["SCORE_JAPANESE"], "JAP_FIRST{$dataRow["SAT_NO"]}", 5, 3, $extra);
                    $data["JAP_SECOND"] = knjCreateTextBox($objForm, $dataRow["SCORE_JAPANESE2"], "JAP_SECOND{$dataRow["SAT_NO"]}", 5, 3, $extra);
                    
                }else{      //表示のみ
                    //英語
                    $data["ENG_FIRST"] = $dataRow["SCORE_ENGLISH"];
                    $data["ENG_SECOND"] = $dataRow["SCORE_ENGLISH2"];
                    
                    //数学
                    $data["MATH_FIRST"] = $dataRow["SCORE_MATH"];
                    $data["MATH_SECOND"] = $dataRow["SCORE_MATH2"];

                    //国語
                    $data["JAP_FIRST"] = $dataRow["SCORE_JAPANESE"];
                    $data["JAP_SECOND"] = $dataRow["SCORE_JAPANESE2"];

                }
                
                //欠席
                $absence = array("0" => "欠", "1" => " ");
                //英語
                $data["ENG_ABS"] = $absence[$dataRow["ABSENCE_ENGLISH"]];
                
                //数学
                $data["MATH_ABS"] = $absence[$dataRow["ABSENCE_MATH"]];
                
                //国語
                $data["JAP_ABS"] = $absence[$dataRow["ABSENCE_JAPANESE"]];
                
                //判定
                $judgeFlg = 0;
                //得点が合わない
                if($dataRow["SCORE_ENGLISH"] != $dataRow["SCORE_ENGLISH2"] ||
                   $dataRow["SCORE_MATH"] != $dataRow["SCORE_MATH2"] ||
                   $dataRow["SCORE_JAPANESE"] != $dataRow["SCORE_JAPANESE2"]){
                    
                        $judgeFlg = 1;
                }
                //欠席してないのに得点が入っていない
                if($dataRow["ABSENCE_ENGLISH"] != 0 && ($dataRow["SCORE_ENGLISH"] == "" || $dataRow["SCORE_ENGLISH2"] == "") ||
                   $dataRow["ABSENCE_MATH"] != 0 && ($dataRow["SCORE_MATH"] == "" || $dataRow["SCORE_MATH2"] == "") ||
                   $dataRow["ABSENCE_JAPANESE"] != 0 && ($dataRow["SCORE_JAPANESE"] == "" || $dataRow["SCORE_JAPANESE2"] == "") ){
                        
                        $judgeFlg = 1;
                }
                //欠席なのに得点入力あり
                if($dataRow["ABSENCE_ENGLISH"] != 1 && ($dataRow["SCORE_ENGLISH"] != "" || $dataRow["SCORE_ENGLISH2"] != "") ||
                   $dataRow["ABSENCE_MATH"] != 1 && ($dataRow["SCORE_MATH"] != "" || $dataRow["SCORE_MATH2"] != "") ||
                   $dataRow["ABSENCE_JAPANESE"] != 1 && ($dataRow["SCORE_JAPANESE"] != "" || $dataRow["SCORE_JAPANESE2"] != "") ){
                        
                        $judgeFlg = 1;
                }
                
                if($judgeFlg != 0){
                    $data["JUDGE"] = "×";
                    $data["color"] = "#fcffcc";
                }else{
                    $data["JUDGE"] = "○";
                    $data["color"] = "#ffffff";
                }
                
                $arg["data"][] = $data;
                
                $model->satNo[] = $data["SAT_NO"];
            }
            $model->messageFlg = 0;
        }else{
            if($model->messageFlg != 0){
                $model->setMessage("更新しました。\\n\\n条件に該当するデータはありません。");
            }else{
                $model->setMessage("該当データはありません。");
            }
            $model->messageFlg = 0;
            //初期化しておく
            $model->satNo = array();
            
        }
        
        Query::dbCheckIn($db);
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);
        
        $arg["REQUESTROOT"] = REQUESTROOT;

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJl376q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl376qForm1.html", $arg);
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    if($model->field["RADIO"] != "1"){
        //更新ボタン
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


}
?>
