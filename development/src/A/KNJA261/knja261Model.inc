<?php

require_once('for_php7.php');


class knja261Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt = array(); // セレクトデータ
    var $selectleft;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja261Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
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

        $this->field = array(
                            "YEAR"              =>  VARS::post("YEAR"),             // 年度
                            "GAKKI"             =>  VARS::post("GAKKI"),            // 学期コード
                            "KUBUN"             =>  VARS::post("KUBUN"),            // 印刷指定
                            "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   // 年組
                            "DATE"              =>  VARS::post("DATE")              // 適用日付
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class' && $this->cmd != 'csv') $this->select_opt = array();
    }

    // 履修科目データＣＳＶ出力
    function getDownloadModel()
    {
        $header = array("YEAR"          => "年度",
                        "SEMESTER"      => "学期",
                        "SCHREGNO"      => "学籍番号",
                        "GRADE"         => "学年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "NAME_SHOW"     => "生徒氏名",
                        "SUBCLASSCD"    => "科目コード",
                        "SUBCLASSNAME"  => "科目名称",
                        "CREDITS"       => "単位数",
                        "CHAIRCD"       => "受講講座コード",
                        "CHAIRNAME"     => "受講講座名称",
                        "AUTHORIZE_FLG" => "半期認定フラグ",
                        "COMP_UNCONDITION_FLG" => "無条件履修修得フラグ");

        $db = Query::dbCheckOut();

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        $query = knja261Query::selectStudySubjectData($this, CTRL_YEAR, $this->field["GAKKI"], $this->selectleft, $this->field["KUBUN"], str_replace("/","-",$this->field["DATE"]));
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",", ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $chr_cnt = $db->getOne(knja261Query::getChairCd($this, $row["YEAR"], $row["SEMESTER"], $row["SCHREGNO"], $row["SUBCLASSCD"], str_replace("/","-",$this->field["DATE"]), "count"));
            $chaircd = $db->getOne(knja261Query::getChairCd($this, $row["YEAR"], $row["SEMESTER"], $row["SCHREGNO"], $row["SUBCLASSCD"], str_replace("/","-",$this->field["DATE"]), "min"));
            $row["CHAIRCD"]  = ($chr_cnt > 1) ? '*'.$chaircd : $chaircd;
            $row["CHAIRNAME"] = $db->getOne(knja261Query::getChairName($row["YEAR"], $row["SEMESTER"], $chaircd));

            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if($header_data)
        {
            $filename = sprintf("%s.csv","履修科目データ");
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
?>
