<?php

require_once('for_php7.php');

class knja611vModel extends Model {
    var $field         = array();
    var $select_data   = array();
    var $opt_subclass  = array();
    var $tableName;
    var $cmd;
    var $control;   //コントロールマスタ

    function knja611vModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHKIND"              =>  VARS::post("SCHKIND"),      //校種
                            "GAKKI"                =>  VARS::post("GAKKI"),        //学期
                            "TESTKINDCD"           =>  VARS::post("TESTKINDCD"),   //テストコード
                            "KESSEKI_FLG"          =>  VARS::post("KESSEKI_FLG"),  //欠席者は「*」を印字する
                            "DBNAME"               =>  VARS::post("DBNAME"));      //DB変更に備えて

        $this->cmd = VARS::request("cmd");

        $this->selectdata = explode(",", VARS::post("selectdata"));

        //初期値の設定
        $this->tableName = 'TESTITEM_MST_COUNTFLG_NEW_SDIV';
        if ($this->Properties["useTestCountflg"]) {
            $this->tableName = $this->Properties["useTestCountflg"];
        }
    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";
        $header = array();

        //ヘッダ作成
        $header["YEAR"]             = "年度";
        $header["SCHREGNO"]         = "学籍番号";
        $header["NAME_SHOW"]        = "生徒氏名";

        $subclassArray = array("SCORE" => '', "AVG" => '平均', "RANK" => '順位', "CNT" => '受験数');
        $this->opt_subclass = array();
        $cnt = 0;

        //科目一覧取得
        $query = knja611vQuery::getSelectSubClass($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($subclassArray as $item => $label) {
                $header[$item.$cnt] = $row["SUBCLASSNAME"].$label;
            }
            $this->opt_subclass[$cnt] = $row["SUBCLASSCD"];
            $cnt++;
        }

        $header["COURSE_AVG"]       = "平均";
        $header["COURSE_RANK"]      = "コース順位";
        $header["COURSE_CNT"]       = "コース受験数";

        //ヘッダセット
        $sep = "";
        foreach ($header as $item => $label) {
            $contents .= $sep.$label;
            $sep = ",";
        }
        $contents .= "\n";

        //データフラグ
        $data = false;

        //データ取得
        $query = knja611vQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);       //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            //データセット
            $sep = "";
            foreach ($header as $item => $label) {
                if (substr($item, 0, 5) == "SCORE" && $row[$item] == "7777") {
                    $row[$item] = "*";      //欠席者「*」
                }
                $contents .= $sep.$row[$item];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //学期名取得
        $semesName = $db->getOne(knja611vQuery::getSemester($this->field["GAKKI"]));
        //テスト名取得
        $testName = $db->getOne(knja611vQuery::getTestItem($this, "TESTNAME"));

        //データベース切断
        Query::dbCheckIn($db);

        if ($data) {
            //ファイル名
            $couse = CTRL_YEAR."年度_".$semesName."_".$testName."_成績連携データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
