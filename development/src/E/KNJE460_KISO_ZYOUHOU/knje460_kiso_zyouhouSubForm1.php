<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460_kiso_zyouhouSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("kiso_zyouhou_select", "POST", "knje460_kiso_zyouhouindex.php", "", "kiso_zyouhou_select");
        // Add by PP for Title 2020-02-03 start
        $arg["TITLE"] = "基礎情報選択画面";
        echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        // Add by PP for Title 2020-02-20 end

        //DB接続
        $db = Query::dbCheckOut();
        
        //年度の設定
        $model->field2["YEAR"] = $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $Row =& $model->field2;
        
        //基礎情報選択項目数
        $query = knje460_kiso_zyouhouQuery::getChallengedSupportBaseInfoYmst($model, 'COUNT');
        $cnt = $db->getOne($query);
        knjCreateHidden($objForm, "SELECT_COUNT", $cnt);  //項目数

        //基礎情報選択を設定
        $query = knje460_kiso_zyouhouQuery::getChallengedSupportBaseInfoYmst($model);
        $result = $db->query($query);
        $idx = 1;
        $kisoZyouhou = ""; //基礎情報
        $line = "\r\n";    //改行
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setVal = "";

            //選択チェックボックス
            $extra  = " id=\"SPRT_SEQ".$idx."\" ";
            if($Row["SPRT_SEQ".$idx] == "1"){
                $extra .= " checked ";
                $kisoZyouhou .= $Row["BASE_TITLE".$idx].$line.$line;
            }
            // Add by PP for PC-Talker 2020-02-03 start
            $sprt_seq = str_replace(array( '(', ')' ), '', $row["BASE_TITLE"]);
            $setVal["SPRT_SEQ"] .= knjCreateCheckBox($objForm, "SPRT_SEQ".$idx , "1", $extra." aria-label=\"$sprt_seq\"");
            // Add by PP for PC-Talker 2020-02-20 end

            //項目名テキスト
            $extra  = " id=\"BASE_TITLE".$idx."\" ";
            $setVal["BASE_TITLE"] .= knjCreateTextArea($objForm, "BASE_TITLE".$idx, 1, 50, "", $extra, $row["BASE_TITLE"]);

            //設定
            $arg["list"][] = $setVal;
            $idx++;
        }
        $result->free();
        
        //選択された基礎情報
        knjCreateHidden($objForm, "HID_KISO_ZYOUHOU", $kisoZyouhou);
        

        //選択された基礎情報をテキストに配置
        if($model->cmd == "subform1_read") $Row["KISO_ZYOUHOU"] = $kisoZyouhou;

        //基礎情報
        if($model->cmd != "subform1_read" && $Row["KISO_ZYOUHOU"] == ""){
            $Row["KISO_ZYOUHOU"] = knje460_kiso_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '02', '01', 'REMARK');
        }
        $extra = " id=KISO_ZYOUHOU aria-label=基礎情報選択全角{$model->kiso_zyouhou_moji}文字X{$model->kiso_zyouhou_gyou}行まで";
        $arg["data"]["KISO_ZYOUHOU"] = KnjCreateTextArea($objForm, "KISO_ZYOUHOU", ($model->kiso_zyouhou_gyou + 1), ($model->kiso_zyouhou_moji * 2 + 1), "soft", $extra, $Row["KISO_ZYOUHOU"]);
        $arg["data"]["KISO_ZYOUHOU_TYUI"] = "(全角{$model->kiso_zyouhou_moji}文字X{$model->kiso_zyouhou_gyou}行まで)";

        Query::dbCheckIn($db);

        //読込ボタンを作成
        // Add by PP for PC-Talker  2020-02-03 start
        $extra = "id=\"btn_read\" onclick=\"return dataPositionSet('{$model->target}');\"";
        $arg["btn_read"] = KnjCreateBtn($objForm, "btn_read", "読込", $extra);
        // Add by PP for PC-Talker 2020-02-20 end


        //戻るボタンを作成する
        // Add by PP for PC-Talker and current cursor in parent page 2020-02-03 start
        $extra = "onclick=\"parent.current_cursor_focus(); return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);
        // Add by PP for PC-Talker and current cursor in parent page 2020-02-20 end

        //hidden
        $nx = 1;
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knje460_kiso_zyouhouSubForm1.html", $arg);
    }
}

?>

