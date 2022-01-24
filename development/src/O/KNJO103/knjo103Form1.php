<?php
class knjo103Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjo103index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();
        
        //学籍番号
        $extra = "";
        $arg["SCHREGNO"] = knjCreateTextBox($objForm, $model->SCHREGNO, "SCHREGNO", 10, 8, $extra);
        
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

        View::toHTML($model, "knjo103Form1.html", $arg);
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
