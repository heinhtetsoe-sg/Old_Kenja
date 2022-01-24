<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjb3053Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjb3053Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
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

        $this->field = array("GRADE"    =>  VARS::post("GRADE")
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    function &getMainModel()
    {
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 10);

        $query = knjb3053Query::getGrade($this, $this->field["GRADE"]);
        $gradeName = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $contents  = ",{$nendo}　{$gradeName["LABEL"]}教科担当者一覧\n";
        $contents .= "学級,担任";

        //科目
        $query = knjb3053Query::getSubclass($this);
        $result = $db->query($query);
        $subclassArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= ",単位,{$row["LABEL"]}";
            $subclassArray[$row["VALUE"]] = $row["VALUE"];
        }
        $result->free();
        $contents .= "\n";

        //単位
        $query = knjb3053Query::getCredits($this);
        $result = $db->query($query);
        $creditArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $creditArray[$row["VALUE"]][$row["COURSE"]] = $row["CREDITS"];
        }
        $result->free();

        //クラス担任
        $query = knjb3053Query::getHrStaff($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= "{$row["HR_NAME"]},{$row["STAFFNAME"]}";
            $courseCode = $row["COURSE"];
            foreach ($subclassArray as $key => $subclassCd) {
                $contents .= ",".$creditArray[$subclassCd][$courseCode].",";
            }
            $contents .= "\n";
        }
        $result->free();

        //ＣＳＶ出力
        $couse = "{$nendo}{$gradeName["LABEL"]}教科担当者一覧";
        $filename = sprintf("%s.csv",$couse);
        common::downloadContents($contents, $filename);
        return true;
    }

}
?>
