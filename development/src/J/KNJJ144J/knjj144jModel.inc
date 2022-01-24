<?php

require_once('for_php7.php');

class knjj144jModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;
    public $selectdata;
    public $lastColumn;//CSVの最後の列の値

    public function knjj144jModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "PRINT_DATE"           =>  VARS::post("PRINT_DATE"),           //出力日
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );
        $this->selectdata   = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function clean()
    {
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";
        //ヘッダ
        $header = array();
        $header["SCHREGNO"]         = "※学籍番号";
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "組";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["PRINT_DATE"]       = "出力日付";
        $header["PRINCIPAL_NAME"]   = "校長名";
        $header[$this->lastColumn]  = $this->lastColumn;

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //データ出力
        $data = false;
        $query = knjj144jQuery::getCsvList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";

            //出力日
            $row["PRINT_DATE"] = str_replace("-", "/", $row["PRINT_DATE"]);
            //校長名(先頭スペース削除)
            $row["PRINCIPAL_NAME"] = preg_replace('/^[ 　]+/u', '', $row["PRINCIPAL_NAME"]);

            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = CTRL_YEAR."年度臘八摂心会皆勤賞";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
