<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knja353Model extends Model
{
    public $year;
    public $lastColumn;  //CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knja353Model()
    {
    }
    public function init()
    {
        $this->year = CTRL_YEAR;
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

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header = array();
        $header["NAME1"]           = "キャンパス";
        $header["SCHOOLNAME1"]     = "区分";
        $header["SCHREGNO"]        = "学籍番号";
        $header["GRADE_NAME1"]     = "学年";
        $header["HR_NAME"]         = "組";
        $header["COURSECODENAME"]  = "コース";
        $header["ATTENDNO"]        = "出席番号";
        $header["NAME"]            = "生徒氏名";
        $header["NAME_KANA"]       = "生徒氏名カナ";
        $header["SEX"]             = "性別";
        $header["BIRTHDAY"]        = "生年月日";
        $header["ZIPCD"]           = "郵便番号";
        $header["ADDR1"]           = "住所1";
        $header["ADDR2"]           = "住所2";
        $header["TELNO"]           = "電話番号";
        $header["ISSUEDATE"]       = "住所・電話番号変更日";
        $header["ISSUEDATE2"]      = "氏名変更日";
        $header["GUARD_NAME"]      = "保護者名";
        $header["GUARD_KANA"]      = "保護者フリガナ";
        $header["ISSUEDATE3"]      = "保護者氏名変更年月日";
        $header["ENT_DATE"]        = "入学日付";
        $header["ENT_DIV"]         = "入学区分";
        $header["GRD_DATE"]        = "卒業(見込)年月日";
        $header["JYOSEKI_DIV"]     = "異動事由";
        $header["JYOSEKI_DATE"]    = "異動年月日";
        $header["TRANSFERCD"]      = "留学区分";
        $header["TRANSFER_SDATE"]  = "留学開始日";
        $header["TRANSFER_EDATE"]  = "留学終了日(復学日)";
        $header["TRANSFERPLACE"]   = "留学情報";
        $header["GENRYUU"]         = "原留情報";
        $header[$this->lastColumn] = $this->lastColumn;
        $contents  = "";    //クリア
        $contents .= implode($header, ",")."\n";
        $header_data = false;

        $db = Query::dbCheckOut();
        $query = knja353Query::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "事務用データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
