<?php

require_once('for_php7.php');
class knji092cModel extends Model
{
    public $field            = array();
    public $select_data      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knji092cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"               =>  VARS::post("YEAR"),                 //年度
                            "SCHOOL_KIND"         =>  VARS::post("SCHOOL_KIND"),          //校種
                            "MIKOMI"              =>  VARS::post("MIKOMI"),               //卒業見込み出力
                            "WRITE_DIV"           =>  VARS::post("WRITE_DIV"),            //縦書き/横書き
                            "ENT_DATE"            =>  VARS::post("ENT_DATE"),             //入学年月日
                            "OUTPUT1"             =>  VARS::post("OUTPUT1"),              //表紙
                            "OUTPUT2"             =>  VARS::post("OUTPUT2"),              //名簿
                            "OUTPUT3"             =>  VARS::post("OUTPUT3"),              //性別
                            "GAKKI"               =>  VARS::post("GAKKI"),                //学期コード
                            "CLASS_MAJOR"         =>  VARS::post("CLASS_MAJOR"),          //クラス・学科
                            "MAJOR_ORDER"         =>  VARS::post("MAJOR_ORDER"),          //学科指定時の五十音順
                            "ENT_GRD_DATE_FORMAT" =>  VARS::post("ENT_GRD_DATE_FORMAT"),  //入学・卒業日付は年月で表示する
                            "PATTERN"             =>  VARS::post("PATTERN"),              //パターン
                            "DBNAME"              =>  VARS::post("DBNAME"),               //DB変更に備えて
                            "SCHOOLCD"            =>  VARS::post("SCHOOLCD")              //校種コード
                            );
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd  = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->useSchregRegdHdat = (strlen($this->Properties["useSchregRegdHdat"])) ? $this->Properties["useSchregRegdHdat"] : 0;

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["GRD_NO"]            = "卒業生台帳番号";
        $header["SCHREGNO"]          = "学籍番号";
        $header["GRADE"]             = "年";
        $header["HR_CLASS"]          = "組";
        $header["ATTENDNO"]          = "出席番号(3年次)";
        $header["NAME"]              = "氏名";
        $header["REAL_NAME"]         = "戸籍氏名";
        $header["BIRTHDAY"]          = "生年月日";
        $header["GRADUATE_DATE"]     = "学校マスタ.卒業日付";
        $header["PRINCIPAL_NAME"]    = "校長名";
        $header["GRADE_RANK"]        = "3年次学年順位";
        $header[$this->lastColumn]   = $this->lastColumn;


        //ヘッダ出力
        $contents .= implode($header, ",")."\n";


        //データ出力
        $data = false;
        $query = knji092cQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= $this->lastColumn;
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->ObjYear."年度得点入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function clean()
    {
    }
}
