<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm050Model extends Model {
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
    function knjm050Model()
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

        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),             //日付
                             "SUBCLASSNAME"         =>  VARS::post("SUBCLASSNAME"),     //科目
                             "HEADER"               =>  VARS::post("HEADER"),           //ヘッダ出力
                             "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
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

        //ヘッダの作成//
        if ($this->Properties["useCurriculumcd"] == "1") {
            $header = array("REPNO"             => "レポート番号",
                            "CLASSCD"           => "教科番号",
                            "SCHOOL_KIND"       => "学校校種",
                            "CURRICULUM_CD"     => "課程コード",
                            "SUBCLASSCD"        => "科目番号",
                            "SUBCLASSNAME"      => "科目名",
                            "SCHREGNO"          => "学籍番号",
                            "RECEIPT_DATE"      => "提出受付日付",
                            "RECEIPT_TIME"      => "提出受付時間",
                            "STAFFCD"           => "添削者職員コード",
                            "GRAD_VALUE"        => "評定",
                            "GRAD_DATE"         => "評価返送日付",
                            "GRAD_TIME"         => "評価返送時間"
                            );
        } else {
            $header = array("REPNO"             => "レポート番号",
                            "SUBCLASSCD"        => "科目番号",
                            "SUBCLASSNAME"      => "科目名",
                            "SCHREGNO"          => "学籍番号",
                            "RECEIPT_DATE"      => "提出受付日付",
                            "RECEIPT_TIME"      => "提出受付時間",
                            "STAFFCD"           => "添削者職員コード",
                            "GRAD_VALUE"        => "評定",
                            "GRAD_DATE"         => "評価返送日付",
                            "GRAD_TIME"         => "評価返送時間"
                            );
        }

        $contents = "" ;
        if ($this->field["HEADER"] == "on"){
            $contents .= implode($header, ",") ."\n";
        }
        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成//
        //SQL文発行
        $query = knjm050Query::selectCsvQuery($this);

        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row2["REPNO"]          = $row["YEAR"].$row["SUBCLASSCD"].sprintf('%02d',$row["STANDARD_SEQ"]).$row["REPRESENT_SEQ"];
            if ($this->Properties["useCurriculumcd"] == "1") {
                $row2["CLASSCD"]        = $row["CLASSCD"];
                $row2["SCHOOL_KIND"]    = $row["SCHOOL_KIND"];
                $row2["CURRICULUM_CD"]  = $row["CURRICULUM_CD"];
            }
            $row2["SUBCLASSCD"]     = $row["SUBCLASSCD"];
            $row2["SUBCLASSNAME"]   = $row["SUBCLASSNAME"];
            $row2["SCHREGNO"]       = $row["SCHREGNO"];
            $row2["RECEIPT_DATE"]   = $row["RECEIPT_DATE"];
            $row2["RECEIPT_TIME"]   = $row["RECEIPT_TIME"];
            $row2["STAFFCD"]        = $row["STAFFCD"];
            $row2["GRAD_VALUE"]     = $row["GRAD_VALUE"];
            $row2["GRAD_DATE"]      = $row["GRAD_DATE"];
            $row2["GRAD_TIME"]      = $row["GRAD_TIME"];
            $contents .= implode($row2, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse = "レポート提出状況";
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
