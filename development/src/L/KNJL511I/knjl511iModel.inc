<?php
//定数定義
class knjl511iModel extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $examno;

    public $examnoKeta;

    public $maxHopeCourseNum;   //志望コース 最大数
    public $maxAltExamnoNum;    //他受験番号 最大数

    //メンバ変数の初期化
    public function knjl511iModel()
    {
    }
    public function init()
    {
        $this->field = array("CON009_REMARK2"    =>  VARS::post("CON009_REMARK2"),   //特待コード
                             "CON009_REMARK3"    =>  VARS::post("CON009_REMARK3"),   //特待理由コード
                             );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = "2";
        $this->examno   = VARS::post("EXAMNO");

        //桁数の設定
        $this->examnoKeta = 10;

        //最大数の設定
        $this->maxHopeCourseNum = 4;
        $this->maxAltExamnoNum = 3;

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();
        //１レコード取得
        $query = knjl511iQuery::getSelectQuery($this, $this->examno);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->cmd=="update" || $this->cmd=="back" || $this->cmd=="next") {
            //リストより選択していない
            if ($this->examno == "") {
                $this->setWarning("MSG308");
                return false;
            }

            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }

            //必須チェック
            if (trim($this->examno) == "") {
                $this->setWarning("MSG301", "(受験番号)");
                return false;
            }
            if (trim($this->field["CON009_REMARK2"]) != "" && trim($this->field["CON009_REMARK3"]) == "") {
                $this->setWarning("MSG301", "(特待理由コード)"); //特待コード有り 特待理由コード無し
                return false;
            }
            if (trim($this->field["CON009_REMARK2"]) == "" && trim($this->field["CON009_REMARK3"]) != "") {
                $this->setWarning("MSG301", "(特待コード)"); //特待コード無し 特待理由コード有り
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl511iQuery::getUpdateQuery($this);
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
        }
    }
}
