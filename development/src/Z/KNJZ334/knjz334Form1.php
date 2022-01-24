<?php

require_once('for_php7.php');
class knjz334Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz334index.php", "", "edit");

        $db = Query::dbCheckOut();
        
        
        //登録されてるお知らせを表示
        $today = date("Y-m-d");
        $query = knjz334Query::getOshirase($model, $today);
        $result = $db->query($query);
        
        $oshirase_no = "";
        
        $extra = " style=\"display: block;\"";
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if($oshirase_no != "" && $oshirase_no != $row["OSHIRASE_NO"] && is_array($naiyo)){
                $arg["naiyo"][] = $naiyo;
            }
            
            //<a>タグは悪さをするのではずす。
            $row["ANNOUNCE"] = str_replace("<a>","",$row["ANNOUNCE"]);
            $row["ANNOUNCE"] = str_replace("</a>","",$row["ANNOUNCE"]);
            
            if($oshirase_no == ""){
                //内容が長いときに省略したい
                $cutNaiyo = mb_strimwidth($row["ANNOUNCE"], 0, 30, "...");
                
                $naiyo["NAIYO"] = $cutNaiyo;
                $setSDate = str_replace("-", "/", $row["START_DATE"]);
                $setEDate = str_replace("-", "/", $row["END_DATE"]);
                $naiyo["START_END_DATE"] = $setSDate." ～ ".$setEDate;
                $naiyo["START_END_DATE"] = "<a href=\"knjz334index.php?cmd=choice&OSHIRASE_NO={$row["OSHIRASE_NO"]}\" target=\"right_frame\" {$extra}>{$naiyo["START_END_DATE"]}</a>";
                $naiyo["STAFF"] = $row["STAFFNAME_SHOW"];
                if($row["GROUP_CD"] == ""){
                    $naiyo["GROUP"] = "個人対象";
                }else if($row["GROUP_CD"] != "log"){
                    $naiyo["GROUP"] = $row["GROUP_CD"];
                }else{
                    $naiyo["GROUP"] = "login";
                }
            }else if($oshirase_no != "" && $oshirase_no != $row["OSHIRASE_NO"]){
                //内容が長いときに省略したい
                $cutNaiyo = mb_strimwidth($row["ANNOUNCE"], 0, 30, "...");
                
                $naiyo["NAIYO"] = $cutNaiyo;
                $setSDate = str_replace("-", "/", $row["START_DATE"]);
                $setEDate = str_replace("-", "/", $row["END_DATE"]);
                $naiyo["START_END_DATE"] = $setSDate." ～ ".$setEDate;
                $naiyo["START_END_DATE"] = "<a href=\"knjz334index.php?cmd=choice&OSHIRASE_NO={$row["OSHIRASE_NO"]}\" target=\"right_frame\" {$extra}>{$naiyo["START_END_DATE"]}</a>";
                $naiyo["STAFF"] = $row["STAFFNAME_SHOW"];
                if($row["GROUP_CD"] == ""){
                    $naiyo["GROUP"] = "個人対象";
                }else if($row["GROUP_CD"] != "log"){
                    $naiyo["GROUP"] = $row["GROUP_CD"];
                }else{
                    $naiyo["GROUP"] = "login";
                }
            }else{
                if($row["GROUP_CD"] == ""){
                    $naiyo["GROUP"] = "個人対象";
                }else if($row["GROUP_CD"] != "log"){
                    $naiyo["GROUP"] .= " /".$row["GROUP_CD"];
                }else{
                    $naiyo["GROUP"] .= " /login";
                }
            }
            
            $oshirase_no = $row["OSHIRASE_NO"];
            
        }
        if($oshirase_no != "" && is_array($naiyo)){
            $arg["naiyo"][] = $naiyo;
        }

        
        //$arg["link"] = REQUESTROOT."/Z/KNJz334_2/knjz334_2index.php";
        $result->free();
        Query::dbCheckIn($db);
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz334Form1.html", $arg);
    }
} 
?>
