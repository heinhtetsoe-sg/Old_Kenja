<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm060wModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjm060wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
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

        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),         //年度
                             "SUBCLASSNAME"         =>  VARS::post("SUBCLASSNAME"), //科目
                             "TESTCD"               =>  VARS::post("TESTCD"),       //テスト
                             "OUTDIV"               =>  VARS::post("OUTDIV"),       //出力区分
                             "HEADER"               =>  VARS::post("HEADER"),       //ヘッダ出力
                             "DBNAME"               =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * 
     */
    function getDownloadModel()
    {

        $header["CLASSCD"]          = "教科コード";
        $header["SCHOOL_KIND"]      = "学校校種";
        $header["CURRICULUM_CD"]    = "課程コード";
        $header["SUBCLASSCD"]       = "科目コード";
        $header["SUBCLASSNAME"]     = "科目名";
        $header["SCHREGNO"]         = "学籍番号";
        $header["NAME"]             = "氏名";

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjm060wQuery::getTest($this);
        $result = $db->query($query);
        $this->testArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header["TEST_".$row["VALUE"]]   = $row["LABEL"];
            $this->testArray[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        if ($this->field["OUTDIV"] == 0 || $this->field["OUTDIV"] == 2) {
            $header["GRAD_VALUE2"]       = "学年成績";
            $header["GRAD_VALUE"]        = "学年評定";
        }

        $contents = "" ;
        if ($this->field["HEADER"] == "on"){
            $contents .= implode($header, ",") ."\n";
        }

        //出力データの作成//
        //SQL文発行
        $query = knjm060wQuery::selectCsvQuery($this);

        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse = "個人別科目別成績一覧表";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }

}
?>
