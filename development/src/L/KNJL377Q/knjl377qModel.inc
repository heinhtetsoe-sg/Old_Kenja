<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl377qModel extends Model {
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
    function knjl377qModel()
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
        
        if($this->cmd == "" || $this->cmd == "import"){
            $this->mode = "";
        }else if($this->cmd == "sat"){
            $this->mode = "1";
        }else if($this->cmd == "sat_connect"){
            $this->mode = "2";
        }else if($this->cmd == "birth"){
            $this->mode = "3";
        }else if($this->cmd == "sex"){
            $this->mode = "4";
        }else if($this->cmd == "sch"){
            $this->mode = "5";
        }


    }
    function &getMainModel()
    {
    }

    function &getImportModel()
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
        
        //取り込む前にSAT_HOPE_WRK_DATのデータを削除する
        $db = Query::dbCheckOut();
        $query = "DELETE FROM SAT_HOPE_WRK_DAT";
        $db->query($query);
        
        if (is_uploaded_file($this->file["tmp_name"])) {

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 1;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    /*headerありにするなら
                    if ($line == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }*/
                    
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    //$tmp    = common::csv2array($buffer);
                    $buffer = str_replace("\"", "",$buffer);
                    $buffer = str_replace("↓", "\n",$buffer);
                    $tmp = explode(",", $buffer);

                    
                    $data = array("RECNO"       =>  $i,
                                  "SAT_NO"      =>  sprintf("%05d", $tmp[0]),
                                  "BIRTHDAY"    =>  date("Y-m-d", strtotime(str_replace("/", "-", $tmp[1]."-".$tmp[2]."-".$tmp[3]))),
                                  "SEX"         =>  $tmp[4],
                                  "GRADE"       =>  $tmp[5],
                                  "FINSCHOOL"   =>  sprintf("%05d", $tmp[6]),
                                  "HOPE1"       =>  sprintf("%05d", $tmp[7]),
                                  "HOPE2"       =>  sprintf("%05d", $tmp[8]),
                                  "HOPE3"       =>  sprintf("%05d", $tmp[9]),
                                  "REGISTERCD"  =>  STAFFCD,
                                  "UPDATED"     =>  "sysdate()");
                    
                    common::_update("SAT_HOPE_WRK_DAT", "", "", $data, $db, "2");
                    
                    $data = array();
                    $i++;
                    
                }
            }
            fclose($fp);
            
            Query::dbCheckIn($db);
            
            $this->setMessage("データを取込みしました。\\n修正後データ作成を押してください。");
            return true;

        } else {
            $this->warning = "ファイル名が不正です。";
            return false;
        }
        

    }
    
    //修正
    function getWrkUpdateModel()
    {
        $db = Query::dbCheckOut();
        
        if($this->mode == "1" || $this->mode == "2"){
            $error = "SAT_NO";
        }else if($this->mode == "3"){
            $error = "BIRTHDAY";
        }else if($this->mode == "4"){
            $error = "SEX";
        }else if($this->mode == "5"){
            $error = "FINSCHOOL";
        }
        
        $this->update = array();
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        foreach($this->recNo as $val){
            
            $updateData = VARS::post("ERROR".$val);
            
            if($this->mode == "3"){
                $updateData = str_replace("/", "-", $updateData);
            }
            
            $this->update[$error] = $updateData;
            
            $where = " WHERE RECNO = '".$val."' ";
            
            common::_update("SAT_HOPE_WRK_DAT", "", $where, $this->update, $db, "1");
            
        }
        
        Query::dbCheckIn($db);
        
        $this->setMessage("修正しました。");
    }
    
    //SAT_APP_FORM_MST更新
    function getUpdateModel()
    {
        $db = Query::dbCheckOut();
        
        //更新データ取得
        $query = knjl377qQuery::getUpdateData();
        $db->query($query);
        
        Query::dbCheckIn($db);
        
        $this->setMessage("更新しました。");
    }

    //CSV出力
    function getExecModel()
    {
        $header_data = "";
        
        $db = Query::dbCheckOut();
        
        //データカウント
        $mode = "1";
        $query = knjl377qQuery::getExportData($mode);
        $cnt = $db->getOne($query);
        
        if($cnt > 0){
            //$contents  = "受験番号,生年,生月,生日,性別,学年,中学コード,志望校１,志望校２,志望校３\n";
            //$contents = "";
            
            $query = knjl377qQuery::getExportData();
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                
                $contents .= $row["SAT_NO"].",".$row["BIRTH_YEAR"].",".$row["BIRTH_MONTH"].",".$row["BIRTH_DAY"].",".$row["SEX"].",".$row["NAME2"].",".$row["SCHOOLCD"].",".$row["HOPECD1"].",".$row["HOPECD2"].",".$row["HOPECD3"]."\n";
                
                $header_data = true;
            }
            
            
        }
        
        Query::dbCheckIn($db);
        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","志望校データ");
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
