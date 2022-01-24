<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd120oModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $term;
    public $gen_ed = "900100";        //総合学習コード
    public $usePerfect;

    /**
     * メンバ変数の初期化
     */
    public function knjd120oModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->cmd     = VARS::request("cmd");
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } elseif ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        //プロパティファイル
        $this->getPropertiesAll();

        for ($i=1; $i<=4; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        for ($i=1; $i<=3; $i++) {
            $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数
        $this->getProperties();
    }

    public function getProperties()
    {
        $posFlg2 = false;
        $retVal = "";
        
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp, 1024)) {
            $pos = strpos($line, "usePerfect");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                if (!$posFlg2) {
                    $this->usePerfect = false;
                }
            } else {
                $retVal = str_replace("usePerfect = ", "", $line);
                $retVal = str_replace("\r\n", "", $retVal);
                $retVal = str_replace("\r", "", $retVal);
                $retVal = str_replace("\n", "", $retVal);
                $this->usePerfect = $retVal;
                $posFlg2 = true;
            }
        }
        fclose($fp);

        return $retVal;
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        /*  更新処理 */

        //学期成績データ項目名を配列にする。
        $term = array("SEM1_INTR_SCORE",
                      "SEM1_TERM_SCORE",
                      "SEM1_VALUE",
                      "SEM2_INTR_SCORE",
                      "SEM2_TERM_SCORE",
                      "SEM2_VALUE",
                      "PROV_FLG",
                      "GRAD_VALUE");

        //初期化
        $this->fields = array();
        for ($j=0; $j < get_count($term); $j++) {
            for ($i=0; $i<$this->field["COUNT"]; $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);

                $this->fields[$term[$j]][]       = $post_term;
            }
        }
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
        }

        knjd120oQuery::update($this);
        knjd120oQuery::updateExecuted($this);
        knjd120oQuery::updateExecuted2($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
