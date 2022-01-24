<?php
class knjo112Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjo112index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();
        
        //学籍番号
        $extra = "";
        $arg["SCHREGNO"] = knjCreateTextBox($objForm, $model->SCHREGNO, "SCHREGNO", 10, 8, $extra);
        
        //送付先情報入力選択ラジオ
        $opt = array(1, 2, 3, 4);
        $name = array("SEND1" => "なし",
                      "SEND2" => "教育委員会",
                      "SEND3" => "学校",
                      "SEND4" => "両方");
        $extraRadio = array("id=\"SEND1\" onchange=\"btn_submit('change');\"",
                            "id=\"SEND2\" onchange=\"btn_submit('change');\"",
                            "id=\"SEND3\" onchange=\"btn_submit('change');\"",
                            "id=\"SEND4\" onchange=\"btn_submit('change');\"");
        $radioArray = knjCreateRadio($objForm, "SEND", $model->SEND, $extraRadio, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"{$key}\">".$name[$key]."</LABEL>";
        
        //入力域
        if($model->SEND != "1"){
            $arg["SENDDATA"] = "1";
            
            if($model->SEND == "4"){
                $arg["KYOUIKU"] = 1;
                $arg["SCHOOL"] = 1;
            }else if($model->SEND == "3"){
                $arg["SCHOOL"] = 1;
            }else if($model->SEND == "2"){
                $arg["KYOUIKU"] = 1;
            }
        }
        
        if($arg["KYOUIKU"] != ""){
            //ID
            $extra = " style=\"margin:2px;\"";
            $kyouiku["K_ID"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["K_ID"], "K_ID", 30, 20, $extra));
            //教育委員会名
            $kyouiku["K_NAME"] = knjCreateTextBox($objForm, $model->field["K_NAME"], "K_NAME", 60, 60, $extra);
            //部課係
            $kyouiku["K_BKK"] = knjCreateTextBox($objForm, $model->field["K_BKK"], "K_BKK", 60, 60, $extra);
            //住所コード
            $kyouiku["K_ADDRCD"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["K_ADDRCD"], "K_ADDRCD", 40, 30, $extra));
            //郵便番号
            $kyouiku["K_ZIPCD"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["K_ZIPCD"], "K_ZIPCD", 30, 20, $extra));
            //住所
            $kyouiku["K_ADDRESS"] = knjCreateTextBox($objForm, $model->field["K_ADDRESS"], "K_ADDRESS", 60, 100, $extra);
            //方書
            $kyouiku["K_KATAGAKI"] = knjCreateTextBox($objForm, $model->field["K_KATAGAKI"], "K_KATAGAKI", 60, 100, $extra);
            //電話番号
            $kyouiku["K_TELNO"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["K_TELNO"], "K_TELNO", 30, 15, $extra));
            //職
            $kyouiku["K_SYOKU"] = knjCreateTextBox($objForm, $model->field["K_SYOKU"], "K_SYOKU", 30, 20, $extra);
            
            $arg["data"] = $kyouiku;
        }
        if($arg["SCHOOL"] != ""){
            //ID
            $extra = " style=\"margin:2px;\"";
            $school["G_ID"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["G_ID"], "G_ID", 30, 20, $extra));
            //学校名
            $school["G_NAME"] = knjCreateTextBox($objForm, $model->field["G_NAME"], "G_NAME", 60, 60, $extra);
            //住所コード
            $school["G_ADDRCD"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["G_ADDRCD"], "G_ADDRCD", 40, 30, $extra));
            //郵便番号
            $school["G_ZIPCD"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["G_ZIPCD"], "G_ZIPCD", 30, 20, $extra));
            //住所
            $school["G_ADDRESS"] = knjCreateTextBox($objForm, $model->field["G_ADDRESS"], "G_ADDRESS", 60, 100, $extra);
            //方書
            $school["G_KATAGAKI"] = knjCreateTextBox($objForm, $model->field["G_KATAGAKI"], "G_KATAGAKI", 60, 100, $extra);
            //電話番号
            $school["G_TELNO"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["G_TELNO"], "G_TELNO", 30, 15, $extra));
            //職
            $school["G_SYOKU"] = knjCreateTextBox($objForm, $model->field["G_SYOKU"], "G_SYOKU", 30, 20, $extra);
            //備考
            $school["G_BIKOU"] = knjCreateTextBox($objForm, $model->field["G_BIKOU"], "G_BIKOU", 60, 100, $extra);
            //分校ID
            $school["G_BUNID"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["G_BUNID"], "G_BUNID", 30, 20, $extra));
            //分校名
            $school["G_BUNNAME"] = knjCreateTextBox($objForm, $model->field["G_BUNNAME"], "G_BUNNAME", 60, 60, $extra);
            //住所コード
            $school["G_BUNADDRCD"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["G_BUNADDRCD"], "G_BUNADDRCD", 40, 30, $extra));
            //郵便番号
            $school["G_BUNZIPCD"] = str_replace("text", "tel", knjCreateTextBox($objForm, $model->field["G_BUNZIPCD"], "G_BUNZIPCD", 30, 20, $extra));
            //住所
            $school["G_BUNADDRESS"] = knjCreateTextBox($objForm, $model->field["G_BUNADDRESS"], "G_BUNADDRESS", 60, 100, $extra);
            //方書
            $school["G_BUNKATAGAKI"] = knjCreateTextBox($objForm, $model->field["G_BUNKATAGAKI"], "G_BUNKATAGAKI", 60, 100, $extra);
            
            $arg["data2"] = $school;
        }

        //ボタン作成
        makeButton($objForm, $arg, $model, $db);


        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]    = $objForm->get_finish();

        View::toHTML($model, "knjo112Form1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model, $db)
{
    //読み込み実行ボタン
    $extra = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "実 行", $extra);


    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function sortChange($array, $i, $mo){
    $i++;

if($i==1){
    print_r($mo->sort{$i});
    echo "<BR>";
}

    $cnt = 0;       //foreachに入った回数
    
    foreach($array as $mo->key{$i} => $mo->val{$i}){
        
        if(is_array($mo->val{$i})){
            ksort($mo->val{$i});
            
            $contents .= sortChange($mo->val{$i}, $i, $mo);
        }else{
            $cnt++;
            $contents .= "\$data";
            $count = 0;
            
            for($j=0; $j < $i; $j++){
                $count++;
                if($j == 0){
                    $contents .= "[\"".$mo->sort{$j}[$mo->key{$count}]."\"]";
                }else{
                echo "j=".$j."<BR>";
                echo "count=".$count."<BR>";
                echo $mo->key{$count-1}."<BR>";
                echo $mo->sort{$j}[$mo->sort{$j-1}[$mo->sort{$j-2}[$mo->key{$count-2}]][$mo->key{$i}]][$mo->key{$i}]."<BR>";
                    $contents .= "[\"".$mo->sort{$j}[$mo->sort{$j-1}[$mo->key{$count-1}]][$mo->key{$i}]."\"]";
                }
            }
            
            $contents .= "=\"".$mo->val{$i}."\";<BR>";
        }
    }
    $i--;
    return $contents;
}


?>
