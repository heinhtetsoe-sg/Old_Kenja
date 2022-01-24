<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl388qModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjl388qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->file = VARS::file("FILE");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル


    }
    function &getMainModel()
    {
    }
    
    //CSV出力
    function getCsvFile()
    {
        $header_data = true;
        $contents  = "受験番号,姓,名,かな姓,かな名,性別,生年月日,学年,中学コード,会場コード,団体コード\n";
        $contents .= "99999,駿台,太郎,すんだい,たろう,1,2000-05-05,09,48999,19,48001\n";
        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","実戦模試海外受験生");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //実行
    function getExecModel()
    {
        $db = Query::dbCheckOut();
        
        //受験番号帯取得
        $query = knjl388qQuery::getExamNo();
        $result = $db->query($query);
        $this->sat = array();
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->sat[$row["PLACECD"]][$row["IND_KUBUN"]]["FROM"] = $row["JUKEN_NO_FROM"];
            $this->sat[$row["PLACECD"]][$row["IND_KUBUN"]]["TO"] = $row["JUKEN_NO_TO"];
        }
        //学年
        $query = knjl388qQuery::getGrade();
        $result = $db->query($query);
        $this->grade = array();
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->grade[] = $row["NAMECD2"];
        }
        
        //中学コード
        $query = knjl388qQuery::getFinschool();
        $result = $db->query($query);
        $this->finschool = array();
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->finschool[] = $row["FINSCHOOLCD"];
        }
        
        //会場コード
        $query = knjl388qQuery::getPlace();
        $result = $db->query($query);
        $this->place = array();
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->place[] = $row["PLACECD"];
        }
        
        
        //団体コード
        $query = knjl388qQuery::getGroup();
        $result = $db->query($query);
        $this->group = array();
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->group[] = $row["GROUPCD"];
        }
        
        
        Query::dbCheckIn($db);
        
        //エラー
        $this->error = array();
        

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        if(get_count($this->error)>0){
            $cnm = "";
            $error = "";
            $this->warning = "データに不備があります。";
            
            foreach($this->error as $val){
                $this->warning .= "\\n行：".$val." 受験番号：".$this->data_arr[$val]["SAT_NO"];
            }

            return false;
        }else if(get_count($this->data_arr) > 0){
            //更新
            $this->UpdateSatAppFormMst();
            
            $this->warning = "更新しました";
            
            return true;
        }else{
            $this->warning = "データに不備があります。";
            return false;
        }
        
        $this->setMessage("作成しました。");
        
    }
    
    //ファイルの文字列の取得
    function GetFileString()
    {
        $temp_arr = array();
        
        
        if (is_uploaded_file($this->file["tmp_name"])) {

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    /*headerありにするなら*/
                    if ($line == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    //$tmp    = common::csv2array($buffer);
                    $buffer = str_replace("\"", "",$buffer);
                    $buffer = str_replace("↓", "\n",$buffer);
                    $tmp = explode(",", $buffer);

                    if($tmp[0] != "99999"){     //サンプルデータ以外
                        $data = array("SAT_NO"      =>  sprintf("%05d", $tmp[0]),
                                      "LASTNAME"    =>  $tmp[1],
                                      "FIRSTNAME"   =>  $tmp[2],
                                      "LASTKANA"    =>  mb_convert_kana($tmp[3],'RNHVc'),
                                      "FIRSTKANA"   =>  mb_convert_kana($tmp[4],'RNHVc'),
                                      "SEX"         =>  $tmp[5],
                                      "BIRTHDAY"    =>  date("Y-m-d", strtotime(str_replace("/", "-", $tmp[6]))),
                                      "GRADE"       =>  sprintf("%02d", $tmp[7]),
                                      "SCHOOLCD"    =>  "2".sprintf("%06d", $tmp[8]),
                                      "PLACECD"     =>  sprintf("%02d", $tmp[9]),
                                      "GROUPCD"     =>  sprintf("%05d", $tmp[10]));
                        //GROUPCDがNULLはNULLにもどす
                        if($data["GROUPCD"] == "00000"){
                            $data["GROUPCD"] = "";
                        }
                        
                        //エラーチェック    データはすべて読み込む
                        $errorCheck = $this->errCheck($data);  //returnはfalseまたはIND_KUBUN
                        if ($errorCheck == false) {
                            //エラーチェックだめだったら
                            $this->error[$i] = $line;
                            $i++;
                        }else{
                            //エラーチェックが大丈夫だったらIND_KUBUNを判断
                            $data["IND_KUBUN"] = $errorCheck;
                        }
                        $temp_arr[$line] = $data;
                    }
                    $line++;
                }
            }
            fclose($fp);

        } else {
            $this->warning = "ファイル名が不正です。";
        }
        
        
        return $temp_arr;
    }
    
    //エラーチェック
    function errCheck($data)
    {
        
        $indNo = "";
        
        //性別
        if($data["SEX"] != "1" && $data["SEX"] != "2"){
            return false;
        }
        
        //生年月日
        list($y, $m, $d) = explode("-", $data["BIRTHDAY"]);
        if($y != "" && $m != "" && $d != ""){
            if(checkdate($m, $d, $y) === false){
                return false;
            }
        }else{
            return false;
        }
        
        //学年
        if(!in_array($data["GRADE"], $this->grade)){
            return false;
        }
        
        //中学コード
        if(!in_array($data["SCHOOLCD"], $this->finschool)){
            return false;
        }
        
        //会場コード
        if(!in_array($data["PLACECD"], $this->place)){
            return false;
        }
        
        //団体コード
        if(!in_array($data["GROUPCD"], $this->group) && $data["GROUPCD"] != ""){
            return false;
        }
        
        //$model->satの中のFROM・TOの範囲内に番号が含まれているか
        foreach($this->sat[$data["PLACECD"]] as $key => $val){
            if($data["SAT_NO"] >= $val["FROM"] && $data["SAT_NO"] <= $val["TO"]){
                $indNo = $key;
            }
        }
        if($indNo != ""){
            return $indNo;
        }else{
            return false;
        }
        
    }
    
    //更新
    function UpdateSatAppFormMst()
    {
        $db = Query::dbCheckOut();
        
        //SAT_EXAM_DATのデータ削除
        $delquery = knjl388qQuery::deleteExam();
        $db->query($delquery);
        //SAT_APP_FORM_MSTのデータ削除
        $query = knjl388qQuery::delSatAppForm();
        $db->query($query);
        
        //SAT_APP_FORM_MSTにデータ追加
        $this->insert = array();
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        
        $this->insert["YEAR"] = CTRL_YEAR;
        $this->insert["KUBUN"] = "S";
        $this->insert["PREFCD"] = "48";
        $this->insert["TOKU_FLG"] = "0";
        $this->insert["ABSENCE"] = "1";
        
        $this->insert["INOUT_KUBUN"] = "4";
        $this->insert["SEND_KUBUN"] = "1";
        
        $this->insert["INPUT_DATE"] = CTRL_DATE;
        
        $this->examInsert = array();
        foreach($this->data_arr as $val){
            
            $this->insert["SAT_NO"] = $val["SAT_NO"];
            
            $this->insert["NAME1"] = $val["LASTNAME"]."　".$val["FIRSTNAME"];
            $this->insert["NAME2"] = $val["LASTNAME"].$val["FIRSTNAME"];
            $this->insert["LASTNAME"] = $val["LASTNAME"];
            $this->insert["FIRSTNAME"] = $val["FIRSTNAME"];
            $this->insert["KANA1"] = $val["LASTKANA"];
            $this->insert["KANA2"] = $val["FIRSTKANA"];
            
            $this->insert["SEX"] = $val["SEX"];
            $this->insert["SCHOOLCD"] = $val["SCHOOLCD"];
            $this->insert["BIRTHDAY"] = $val["BIRTHDAY"];
            $this->insert["GRADUATION"] = $val["GRADE"];
            
            $this->insert["GROUPCD"] = $val["GROUPCD"];
            $this->insert["PLACECD"] = $val["PLACECD"];
            
            $this->insert["IND_KUBUN"] = $val["IND_KUBUN"];
            
            common::_update("SAT_APP_FORM_MST", "", "", $this->insert, $db, "2");
            
            //SAT_EXAM_DATも作成したい
            $this->examInsert["REGISTERCD"] = STAFFCD;
            $this->examInsert["UPDATED"] = "sysdate()";
            $this->examInsert["YEAR"] = CTRL_YEAR;
            $this->examInsert["SAT_NO"] = $this->insert["SAT_NO"];
            $this->examInsert["ABSENCE"] = "1";
            $this->examInsert["ABSENCE_ENGLISH"] = "1";
            $this->examInsert["ABSENCE_MATH"] = "1";
            $this->examInsert["ABSENCE_JAPANESE"] = "1";
            if($this->insert["PLACECD"] == "80" && $this->insert["SCHOOLCD"] == "2008005"){ //駿中生
                $this->examInsert["AREA"] = "1";
            }else if($this->insert["INOUT_KUBUN"] == "1"){  //県内
                $this->examInsert["AREA"] = "2";
            }else{          //県外
                $this->examInsert["AREA"] = "3";
            }
            
            common::_update("SAT_EXAM_DAT", "", "", $this->examInsert, $db, "2");
        }
        
        
        Query::dbCheckIn($db);
    }


}
//var_dump($_REQUEST);
?>
