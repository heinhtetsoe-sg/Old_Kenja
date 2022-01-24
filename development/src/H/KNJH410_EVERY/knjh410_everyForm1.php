<?php

require_once('for_php7.php');

class knjh410_everyForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh410_everyindex.php", "", "edit");

        $db = Query::dbCheckOut();
        
        //選択した生徒の年組番名前表示
        $query = knjh410_everyQuery::getSchregData($model->schregno);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["GRADE"] = $row["HR_NAME"]." ".number_format($row["ATTENDNO"])."番";
        $arg["NAME"] = $model->schregno."：".$row["NAME"];
        
        
        //検索用
        //日付
        $arg["FROM_DATE"] =  View::popUpCalendar($objForm, "FROM_DATE", str_replace("-","/",$model->search["FROM_DATE"]),"");
        $arg["TO_DATE"]   =  View::popUpCalendar($objForm, "TO_DATE", str_replace("-","/",$model->search["TO_DATE"]),"");
        
        //入力者
        $extra = "";
        $query = knjh410_everyQuery::getUpdateStaff($model->schregno);
        $result = $db->query($query);
        $opt = array();
        $opt[0] = array("value" =>  "",
                        "label" =>  "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  =>  $row["REGISTERCD"],
                           "label"  =>  $row["REGISTERCD"]."：".$row["STAFFNAME_SHOW"]);
        }
        $arg["STAFF"] = knjCreateCombo($objForm, "STAFF", $model->search["STAFF"], $opt, $extra, "1");
        
        //項目(タグ)
        $extra = "";
        $arg["TAGSEARCH"] = knjCreateTextBox($objForm, $model->search["TAGSEARCH"], "TAGSEARCH", 35, 20, $extra);
        
        //タグの頻出データを表示
        $query = knjh410_everyQuery::getTagCnt($moel->schregno);
        $result = $db->query($query);
        $sp = "";
        $linkData = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $linkData .= $sp."<a onclick=\"clickTag('".$row["TAG"]."');\">#".$row["TAG"]."</a>";
            $sp = "　";
        }
        $arg["TAGLINK"] = $linkData;

        //項目の検索またはorかつ
        $opt3 = array(1, 2);
        $extra = array("id=\"SEARCH1\"\"", "id=\"SEARCH2\"\"");
        $label2 = array("SEARCH1" => "または", "SEARCH2" => "かつ");
        $radioArray = knjCreateRadio($objForm, "SEARCH", $model->search["SEARCH"], $extra, $opt3, get_count($opt3));
        $radioData = "";
        $sp = "";
        foreach($radioArray as $key => $val){
            $radioData .= $sp.$val."<LABEL for=\"".$key."\">".$label2[$key]."</LABEL>";
            $sp = "　";
        }
        $arg["SEARCH"] = $radioData;
        
        //項目の検索範囲
        $opt2 = array(1, 2, 3);
        $extra = array("id=\"SEARCHRADIO1\"\"", "id=\"SEARCHRADIO2\"\"", "id=\"SEARCHRADIO3\"\"");
        $label = array("SEARCHRADIO1" => "全一致", "SEARCHRADIO2" => "前方一致", "SEARCHRADIO3" => "部分一致");
        $radioArray = knjCreateRadio($objForm, "SEARCHRADIO", $model->search["SEARCHRADIO"], $extra, $opt2, get_count($opt2));
        $radioData = "";
        $sp = "";
        foreach($radioArray as $key => $val){
            $radioData .= $sp.$val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
            $sp = "　";
        }
        $arg["SEARCHRADIO"] = $radioData;
        
        
        //検索結果
        if($model->search["TAGSEARCH"] != ""){
            //タグテーブル優先にして検索
            $query = knjh410_everyQuery::getSearchTag($model->schregno, $model->search);
        }else{
            //内容テーブル優先に検索
            $query = knjh410_everyQuery::getSearch($model->schregno, $model->search);
        }
        $result = $db->query($query);
        
        $recNo = "";
        $data = array();
        $sp = "";
        $createFlg = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if($recNo == "" || $recNo != $row["RECNO"]){
                if(!empty($data)){
                    $arg["data"][] = $data; //一つ前のデータを作成
                    $data = array();
                    $sp = "";
                }
                
                $data["FIND_DATE"] = str_replace("-", "/", $row["FIND_DATE"]);
                if($row["FIND_TIME"] != ""){
                    $data["FIND_DATE"] .= "　".$row["FIND_TIME"];
                }
                if(mb_strlen($row["TITLE"]) > 20){
                    $title = mb_substr($row["TITLE"], 0, 20);
                }else{
                    $title = $row["TITLE"];
                }
                $data["TITLELINK"] = "<a href=\"knjh410_everyindex.php?cmd=choice&RECNO=".$row["RECNO"]."\" target=\"right_frame\" style=\"display: block;\">".$title."</a>";
                
                $data["STAFFNAME"] = $row["STAFFNAME_SHOW"];
            }
            
            //タグ部分作成
            if($row["TAG"] != ""){
                $data["TAGNAME"] .= $sp."#".$row["TAG"];
                $sp = "　";
            }
            
            $recNo = $row["RECNO"];
            
            $createFlg = 1;
        }
        if(!empty($data)){
            $arg["data"][] = $data; //最後のデータを作成
        }
        
        if($createFlg != 1 && $model->cmd != "list"){
            $model->setMessage("対象のデータはありません");
        }
        

        
        $result->free();
        Query::dbCheckIn($db);

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        //hidden作成
        makeHidden($objForm, $model);


        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh410_everyForm1.html", $arg);
    }
}


//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //検索
    $extra = " onclick=\"btn_submit('search');\"";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
    //検索クリア
    $extra = " onclick=\"btn_submit('clear');\"";
    $arg["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "クリア", $extra);
    //終了
    $extra = " onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);  //jsでのエラーチェック用
}
?>
