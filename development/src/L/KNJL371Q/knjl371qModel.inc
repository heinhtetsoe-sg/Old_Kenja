<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl371qModel extends Model {
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
    function knjl371qModel()
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
        
        if($this->cmd == ""){
            $this->error = array();
            $this->data_arr = array();
        }


    }
    function &getMainModel()
    {
    }

    function &getExecModel()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $extension = $fileInfo['extension'];
        $extension = strtoupper($extension);

        //拡張子チェック
        if ($extension != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$extension."ファイルの取込は出来ません。)";
            return false;
        }
        
        //エラー
        $this->error = array();
        

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        if(get_count($this->error)>0){
            return false;
        }else if(get_count($this->data_arr) > 0){
            //更新
            $this->UpdateSatAppForm();
            
            $this->warning = "更新しました";
            
            return true;
        }else{
            return false;
        }

    }
    //ファイルの文字列の取得
    function GetFileString()
    {
        $temp_arr = array();
        
        $db = Query::dbCheckOut();
        
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

                    
                    $data = array("SAT_NO"          =>  $tmp[0],
                                  "NAME"            =>  $tmp[1],
                                  "MOCK_AUG_NO"     =>  $tmp[11],
                                  "MOCK_AUG_DEV"    =>  $tmp[12],
                                  "MOCK_SEP_NO"     =>  $tmp[13],
                                  "MOCK_SEP_DEV"    =>  $tmp[14]);
                    
                    $temp_arr[$line] = $data;

                    //エラーチェック    データはすべて読み込む
                    if (!$this->errCheck($db, $data)) {
                        //エラーチェックだめだったら
                        $this->error[$i] = $line;
                        $i++;
                    }
                    $line++;
                }
            }
            fclose($fp);

        } else {
            $this->warning = "ファイル名が不正です。";
        }
        
        Query::dbCheckIn($db);
        
        return $temp_arr;
    }
    
    //エラーチェック
    function errCheck($db, $data)
    {
        $returnFlg = true;
        
        //SAT_NOがSAT_APP_FORM_MSTに存在するか確認
        $query = knjl371qQuery::checkSatNo($data["SAT_NO"]);
        $cnt = $db->getOne($query);
        if($cnt > 0){
            return true;
        }else{
            return false;
        }
        
    }
    
    //SAT_APP_FORM_MST更新
    function UpdateSatAppForm()
    {
        $db = Query::dbCheckOut();
        
        $this->update = array();
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        foreach($this->data_arr as $key => $val){
            if($val["MOCK_AUG_NO"] != ""){
                $this->update["MOCK_AUG_NO"] = $val["MOCK_AUG_NO"];
                $this->update["MOCK_AUG_DEV"] = $val["MOCK_AUG_DEV"];
            }else{
                $this->update["MOCK_AUG_NO"] = "";
                $this->update["MOCK_AUG_DEV"] = "";
            }
            if($val["MOCK_SEP_NO"] != ""){
                $this->update["MOCK_SEP_NO"] = $val["MOCK_SEP_NO"];
                $this->update["MOCK_SEP_DEV"] = $val["MOCK_SEP_DEV"];
            }else{
                $this->update["MOCK_SEP"] = "";
                $this->update["MOCK_SEP_DEV"] = "";
            }
            
            $where = " WHERE SAT_NO = '".$val["SAT_NO"]."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_APP_FORM_MST", "", $where, $this->update, $db, "1");
            
        }
        
        Query::dbCheckIn($db);
    }

    //CSV出力
    function getExportModel()
    {
        $header_data = "";
        
        $db = Query::dbCheckOut();
        
        //データカウント
        $query = knjl371qQuery::getExportData("1");
        $cnt = $db->getOne($query);
        
        if($cnt > 0){
            $contents = "進適受験番号,氏名,氏名2,フリガナ,性別,中学コード,出身中学校名,生年月日,電話番号,県コード,県名,進8受験番号,進83科偏差値,進9受験番号,進93科偏差値\n";
            
            $query = knjl371qQuery::getExportData();
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                
                $contents .= $row["SAT_NO"].",".$row["NAME1"].",".$row["NAME2"].",".$row["NAME_KANA"].",".$row["SEX"].",".$row["SCHOOLCD"].",".$row["FINSCHOOL_NAME_ABBV"].",";
                $contents .= $row["BIRTHDAY"].",".$row["TELNO1"].",".$row["PREFCD"].",".$row["PREF_NAME"].",,,,\n";
                
                $header_data = true;
            }
            
            
        }
        
        Query::dbCheckIn($db);
        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","リンデン模試");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
//var_dump($_REQUEST);
?>
