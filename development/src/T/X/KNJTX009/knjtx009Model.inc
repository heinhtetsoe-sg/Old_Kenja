<?php

require_once('for_php7.php');

//定数定義
class knjtx009Model extends Model {
    var $cmd;
    var $data;
    var $max;
     //メンバ変数の初期化
    function knjtx009Model()
    {
    }
    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $tmp["TEMP"] = VARS::file("csvfile");
        if ($tmp["TEMP"]["name"] != ""){
            $this->field["CSVFILE"] = VARS::file("csvfile");        //ファイルのアップロード
            //文字コードをEUCにデータコンバート
            //$this->field["CSVFILE"]["name"] = mb_convert_encoding($this->field["CSVFILE"]["name"], "EUC", "SJIS");
            //文字コードをUTF-8にデータコンバート
            $this->field["CSVFILE"]["name"] = mb_convert_encoding($this->field["CSVFILE"]["name"], "UTF-8", "SJIS-win");
        }
    }

    function &getMainModel()
    {
    }

    //実行モデルを返します
    function &getExecuteModel()
    {
        if(!stristr($this->field["CSVFILE"]["name"],"csv")){
            $this->setWarning('MSG203',"不正なファイルです。");
            return false;
        }
        $this->max = knjtx009Query::MaxZipNo($this)."<br>";
        $this->fileUpload();
    }

    function fileUpload()
    {
        $max = $this->max;
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        set_time_limit(0);

        $flag = 0;          //正しいデータかどうかのフラグ
        $citycd = "";       //CITYCDを保持する
        if (is_uploaded_file ($this->field["CSVFILE"]["tmp_name"])){    //is_uploaded_file--アップロードされたファイルか？
            $fp = fopen ($this->field["CSVFILE"]["tmp_name"], "r");     //fopen--読み取り専用でファイルを開く
            while (!feof ($fp)) {                                       //feof--ファイルポインタがファイル終端に達しているか
                $buffer = fgets($fp, 4096);                             //fgets--ファイルポインタから1行取得する
                if (trim($buffer) != ''){
                    //$buffer = mb_convert_encoding($buffer,"EUC","SJIS");
                    $buffer = mb_convert_encoding($buffer,"UTF-8","SJIS-win");
                    $tmp = explode(",", $buffer);                       //explode--文字列を文字列により分割する(",")

                    $max = $max+1;


                    if(is_string(str_replace("\"","",mb_convert_kana($tmp[5],"KV"))) &&
#                       strlen(str_replace("\"","",mb_convert_kana($tmp[5],"KV"))) < 150){
                       strlen(str_replace("\"","",mb_convert_kana($tmp[5],"KV"))) > 150){

                        $tmp[5]=substr(str_replace("\"","",mb_convert_kana($tmp[5],"KV")),0,75);
                    }

                    if(is_string(str_replace("\"","",mb_convert_kana($tmp[8],"KV"))) &&
#                       strlen(str_replace("\"","",mb_convert_kana($tmp[8],"KV"))) < 150){
                       strlen(str_replace("\"","",mb_convert_kana($tmp[8],"KV"))) > 150){

                        $tmp[8]=substr(str_replace("\"","",mb_convert_kana($tmp[8],"KV")),0,75);
                    }

                    $data = array("ZIPNO"      => sprintf("%06d",$max),
                                  "CITYCD"     => $tmp[0],
                                  "OLD_ZIPCD"  => str_replace("\"","",str_replace(" ","",$tmp[1])),
                                  "NEW_ZIPCD"  => str_replace("\"","",$tmp[2]),
                                  "PREF_KANA"  => str_replace("\"","",mb_convert_kana($tmp[3],"KV")),
                                  "CITY_KANA"  => str_replace("\"","",mb_convert_kana($tmp[4],"KV")),
                                  "TOWN_KANA"  => str_replace("\"","",mb_convert_kana($tmp[5],"KV")),
                                  "PREF"       => str_replace("\"","",$tmp[6]),
                                  "CITY"       => str_replace("\"","",$tmp[7]),
                                  "TOWN"       => str_replace("\"","",$tmp[8]) );

                    if (is_numeric($data["CITYCD"]) && strlen($data["CITYCD"]) <= 5 &&
                        is_numeric($data["OLD_ZIPCD"]) && strlen($data["OLD_ZIPCD"]) <= 6 &&
                        is_numeric($data["NEW_ZIPCD"]) && strlen($data["NEW_ZIPCD"]) <= 8 &&
                        is_string($data["PREF_KANA"]) && strlen($data["PREF_KANA"]) <= 75 &&
                        is_string($data["CITY_KANA"]) && strlen($data["CITY_KANA"]) <= 120 &&
                        is_string($data["TOWN_KANA"]) && strlen($data["TOWN_KANA"]) <= 150 &&
                        is_string($data["PREF"]) && strlen($data["PREF"]) <= 75 &&
                        is_string($data["CITY"]) && strlen($data["CITY"]) <= 120 &&
                        is_string($data["TOWN"]) && strlen($data["TOWN"]) <= 150 )
                    {
                        if($data["CITYCD"] != $citycd)
                        {
                            $query = knjtx009Query::getDeleteQuery($data["CITYCD"]);
                            $result = $db->query($query);
                            $citycd = $data["CITYCD"];
                        }
                        if(strlen($data["OLD_ZIPCD"]) > 3)
                        {
                            $top = substr($data["OLD_ZIPCD"],0,3);
                            $end = substr($data["OLD_ZIPCD"],3,2);
                            $data["OLD_ZIPCD"] = $top."-".$end;
                        }
                        if(strlen($data["NEW_ZIPCD"]) > 3)
                        {
                            $top = substr($data["NEW_ZIPCD"],0,3);
                            $end = substr($data["NEW_ZIPCD"],3,4);
                            $data["NEW_ZIPCD"] = $top."-".$end;
                        }
                        $query  = knjtx009Query::insertQueryCsv($data);
                        $result = $db->query($query);
                        $flag = 1;
                     }else{
                        if(is_numeric($data["CITYCD"]) && strlen($data["CITYCD"]) > 5){
                            $str1 = " CITYCD";
                        }else{
                            $str1 = "*CITYCD";
                        }
                        if(is_numeric($data["OLD_ZIPCD"]) && strlen($data["OLD_ZIPCD"]) > 6){
                            $str2 = " OLD_ZIPCD";
                        }else{
                            $str2 = "*OLD_ZIPCD";
                        }
                        if(is_numeric($data["NEW_ZIPCD"]) && strlen($data["NEW_ZIPCD"]) > 8){
                            $str3 = " NEW_ZIPCD";
                        }else{
                            $str3 = "*NEW_ZIPCD";
                        }
                        if(is_string($data["PREF_KANA"]) && strlen($data["PREF_KANA"]) > 75){
                            $str4 = " PREF_KANA";
                        }else{
                            $str4 = "*PREF_KANA";
                        }
                        if(is_string($data["CITY_KANA"]) && strlen($data["CITY_KANA"]) > 120){
                            $str5 = " CITY_KANA";
                        }else{
                            $str5 = "*CITY_KANA";
                        }
                        if(is_string($data["TOWN_KANA"]) && strlen($data["TOWN_KANA"]) > 150){
                            $str6 = " TOWN_KANA";
                        }else{
                            $str6 = "*TOWN_KANA";
                        }
                        if(is_string($data["PREF"]) && strlen($data["PREF"]) > 75){
                            $str7 = " PREF";
                        }else{
                            $str7 = "*PREF";
                        }
                        if(is_string($data["CITY"]) && strlen($data["CITY"]) > 120){
                            $str8 = " CITY";
                        }else{
                            $str8 = "*CITY";
                        }
                        if(is_string($data["TOWN"]) && strlen($data["TOWN"]) > 150){
                            $str9 = " TOWN";
                        }else{
                            $str9 = "*TOWN";
                        }

                        $this->setWarning('MSG203',"データが不正です。\\n\\n               エラーデータ内容\\n ZIPNO [ ".$data["ZIPNO"]." ] \\n".$str1." [ ".$data["CITYCD"]." ] \\n".$str2." [ ".$data["OLD_ZIPCD"]." ] \\n".$str3." [ ".$data["NEW_ZIPCD"]." ] \\n".$str4." [ ".$data["PREF_KANA"]." ] \\n".$str5." [ ".$data["CITY_KANA"]." ] \\n".$str6." [ ".$data["TOWN_KANA"]." ] \\n".$str7." [ ".$data["PREF"]." ] \\n".$str8." [ ".$data["CITY"]." ] \\n".$str9." [ ".$data["TOWN"]." ]");
                        $db->rollback();
                        return false;
                     }
                }
            }
            if($flag == 1){
                $db->commit(); // トランザクションをコミットする。
                Query::dbCheckIn($db);
                fclose($fp);
                $this->setMessage('MSG201');
            }elseif($flag == 0){
                $this->setWarning('MSG203',"不正なファイルです。");
                return false;
            }
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>