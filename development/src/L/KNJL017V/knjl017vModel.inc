<?php
class knjl017vModel extends Model
{
    public $cmd;
    public $field = array();
    public $ObjYear;
    public $selectLeft;
    public $selectLeftText;
    public $selectRight;
    public $selectRightText;

    public function knjl017vModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("EXAM_SCHOOL_KIND"     => VARS::post("EXAM_SCHOOL_KIND"),
                             "EXAM_ID"              => VARS::post("EXAM_ID"),
                             "PLACE_ID"             => VARS::post("PLACE_ID"),
                            );

        $this->selectLeft      = VARS::post("selectLeft");
        $this->selectLeftText  = VARS::post("selectLeftText");
        $this->selectRight     = VARS::post("selectRight");
        $this->selectRightText = VARS::post("selectRightText");
    }

    //入力項目チェック
    public function validateInput($db)
    {
        //データ指定なし
        if (!$this->selectLeft && !$this->selectRight) {
            $this->setWarning("MSG304");
            return false;
        }
        return ture;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if ($this->validateInput($db)) {
            knjl017vQuery::getDeleteQuery($this, $db);
            if ($this->selectLeft) {
                //一覧の更新
                knjl017vQuery::getUpdateQuery($this, $db);
            }

            $this->setMessage("MSG201");
        }
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //CSV出力
    public function getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();

        //ヘッダー
        $header = array();
        $header["YEAR"]                 = "年度";
        $header["PLACE_ID"]             = "会場ID";
        $header["PLACE_NAME"]           = "会場名";
        $header["EXAM_ID"]              = "試験ID";
        $header["EXAM_NAME"]            = "試験名";
        $header["RECEPTNO"]             = "受験番号";
        $header["EXAMNO"]               = "統一志願者ID";
        $header["NAME"]                 = "氏名";

        //初期化
        $contents = ""; //クリア
        $data = false; //出力フラグ

        //ヘッダ
        $contents .= implode(",", $header) . "\n";

        //データ
        $query = knjl017vQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep . $row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        //校種
        $query = knjl017vQuery::getSchoolKind($this->ObjYear, $this->field["EXAM_SCHOOL_KIND"]);
        $schoolKindRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolKindName = $schoolKindRow["NAME1"];
        //試験名
        $query = knjl017vQuery::getExamId($this, $this->field["EXAM_ID"]);
        $examRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $examName = $examRow["EXAM_NAME"];
        //会場名
        $query = knjl017vQuery::getPlaceId($this, $this->field["PLACE_ID"]);
        $hollRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $hollName = $hollRow["PLACE_NAME"];

        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $fname  = $this->ObjYear."年度";
            $fname .= "_会場別受験者CSV";
            $fname .= "_".$schoolKindName;
            $fname .= "_".$examName;
            $fname .= "_".$hollName;
            $filename = sprintf("%s.csv", $fname);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
