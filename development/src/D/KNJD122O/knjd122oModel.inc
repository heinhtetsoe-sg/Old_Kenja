<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd122oModel.inc,v 1.2 2012/06/05 05:31:22 hamasaki Exp $

class knjd122oModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $creditflg;
    var $cmd;
    var $term;
    var $gen_ed = "90";
    var $testcd;//講座コードをどの考査にセットするかを判断するコードの値を保持用 NO001Add
    var $repGetCredit;
    var $testTable;

    /**
     * メンバ変数の初期化
     */
    function knjd122oModel()
    {
        $this->field = array();
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

    }

    function init()
    {
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        //講座コード
        if($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        }else{
            $this->field["CHAIRCD"] = "";
        }

        $this->cmd     = VARS::request("cmd");

        for($i=1;$i<=5;$i++)
        {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");

        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd122oQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];
        //プロパティファイル
        $this->getProperties();
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function getProperties()
    {
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
        while ($line = fgets($fp,1024)) {
            $pos = strpos($line, "useTestCountflg");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
            } else {
                $retVal = str_replace("useTestCountflg = ", "", $line);
                $this->testTable = str_replace("\r\n", "", $retVal);
                break;
            }
        }
        fclose($fp);

        return $retVal;
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        /*  更新処理 */

        //学期成績データ項目名を配列にする。
        $term = array("SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_VALUE","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_TERM2_SCORE","SEM2_TERM2_VALUE","SEM2_VALUE","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");//Modify

        //初期化
        $this->fields = array();
        for ($j=0; $j < get_count($term); $j++) {

            for($i=0;$i<get_count($this->data["SCHREGNO"]);$i++){

                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);

                if(isset($post_term)){

                    //出欠情報が入ってたら成績は空
                    if ($post_term == "-" || $post_term == "=") {
                        $this->fields[$term[$j]."_DI"][] = $post_term;
                        $this->fields[$term[$j]][]       = "";
                    } else {
                        $this->fields[$term[$j]."_DI"][] = "";
                        $this->fields[$term[$j]][]       = $post_term;
                    }

                }else{

                    $this->fields[$term[$j]][]       = $this->data[$term_name];
                    $this->fields[$term[$j]."_DI"][] = $this->attend_data[$term_name];
                }
            }
        }

        knjd122oQuery::update($this);
        knjd122oQuery::updateExecuted($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
?>
