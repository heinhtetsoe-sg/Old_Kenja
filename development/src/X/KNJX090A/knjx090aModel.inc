<?php

require_once('for_php7.php');
class knjx090aModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx090aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array();
        $this->field["GRADE"] = VARS::post("GRADE");    //学年

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    // HRクラスの自動名簿生成
    public function getDownloadModel2()
    {
        // ＣＳＶ項目
        $header = array("YEAR"            => "※年度",
                        "SEMESTER"        => "※学期",
                        "CHAIRCD"         => "※講座コード",
                        "GRADE"           => "学年",
                        "HR_CLASS"        => "組",
                        "ATTENDNO"        => "出席番号",
                        "NAME"            => "氏名",
                        "SCHREGNO"        => "※学籍番号",
                        "APPDATE"         => "※適用開始日付",
                        "APPENDDATE"      => "※適用終了日付",
                        "ROW"             => "座席行",
                        "COLUMN"          => "座席列",
                        $this->lastColumn => $this->lastColumn);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        $query = knjx090aQuery::selectAutoChairStd($this, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // DB追加
            $query = knjx090aQuery::insertAutoChairStd($row, CTRL_YEAR, CTRL_SEMESTER, STAFFCD);
            $db->query($query);
            // CSV出力
            $row = str_replace(",", ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= CTRL_YEAR .",";
            $contents .= CTRL_SEMESTER .",";
            $contents .= $row["CHAIRCD"] .",";
            $contents .= $row["GRADE"] .",";
            $contents .= $row["HR_CLASS"] .",";
            $contents .= $row["ATTENDNO"] .",";
            $contents .= $row["NAME"] .",";
            $contents .= $row["SCHREGNO"] .",";
            $contents .= $row["SDATE"] .",";
            $contents .= $row["EDATE"] .",";
            $contents .= "" .",";
            $contents .= "" .",";
            $contents .= $this->lastColumn ."\n";
            $header_data = true;
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            if (!$this->Properties["useXLS"]) {
                $filename = sprintf("%s.csv", "講座受講生徒データ(自動生成)");
                common::downloadContents($contents, $filename);
                return true;
            } else {
                $this->warning = "データを更新しました。";
                return false;
            }
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
