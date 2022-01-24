<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd120kModel extends Model
{
    public $field     = array();
    public $flg       = array();
    public $data      = array();
    public $std       = array();
    public $testkind  = array();   //NO001
    public $flg_data=array();
    public $cmd;
    public $checkTestscore;
    public $term;
    public $control_cd=array();
    public $syncd;                 //総合的な学習の科目コードを格納
    public $syncdFlg;              //総合的な学習の科目:true それ以外の科目:false


    /**
     * メンバ変数の初期化
     */
    public function knjd120kModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        //実行権限
        $this->sec_competence = AUTHORITY;

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd     = VARS::request("cmd");

        //科目コード
        if (VARS::post("SUBCLASSCD")) {
            $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        } elseif ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        } else {
            $this->field["SUBCLASSCD"] = "";
        }
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } elseif ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }

        //総合的な学習の時間、総合的な探究の時間の科目コード
        $this->syncd = array("900100", "900200");

        //総合的な学習の科目か
        $subcd = "";
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $this->field["SUBCLASSCD"]);
            $subcd = $subclass_array[3];
        } else {
            $subcd = $this->field["SUBCLASSCD"];
        }
        $this->syncdFlg = in_array($subcd, $this->syncd) ? true : false;

        //DB接続
        $db = Query::dbCheckOut();

        //データ取得
        $this->hyouteiABC = array();
        $query = knjd120kQuery::getABCHyoutei();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->hyouteiABC[$row["NAME1"]] = $row["NAME1"];
        }
        $result->free();
        $this->syncdFlg = (in_array($this->field["SUBCLASSCD"], $this->hyouteiABC)) ? true : $this->syncdFlg;

        //DB切断
        Query::dbCheckIn($db);

        $this->control_cd=array();
        //管理者コントロール
        $db = Query::dbCheckOut();
        $query = knjd120kQuery::selectContolCodeQuery($this);
        $result = $db->query($query);
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->control_cd[]=$row["CONTROL_CODE"];
        }
          Query::dbCheckIn($db);

    //学期成績データ項目名を配列にする。
        $term=array("SEM1_INTER_REC","SEM1_TERM_REC","SEM1_REC","SEM2_INTER_REC","SEM2_TERM_REC","SEM2_REC","SEM3_TERM_REC","GRADE_RECORD","PROV_FLG");
    //学期成績データフラグ項目名を配列にする。
        $flg=array("SEM1_INTER_REC_FLG","SEM1_TERM_REC_FLG","SEM1_REC_FLG","SEM2_INTER_REC_FLG","SEM2_TERM_REC_FLG","SEM2_REC_FLG","SEM3_TERM_REC_FLG");

    //初期化
        $this->fields=array();
        $this->flg_data_ar=array();
        for ($j=0; $j<get_count($term); $j++) {
            for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {
                $term_name=$term[$j]."-".$i;
                $flg_name=$flg[$j]."-".$i;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    //出欠情報が入ってたら成績は空
                    if (VARS::post($term_name)=="KK" | VARS::post($term_name)=="KS") {
                        $this->fields[$term[$j]."_DI"][] = $post_term;
                        $this->fields[$term[$j]][] = "";
                    } else {
                        $this->fields[$term[$j]."_DI"][] = "";
                        $this->fields[$term[$j]][] = $post_term;
                    }
                    $this->flg_data_ar[$flg[$j]][] = (VARS::post($term_name)=="" | VARS::post($term_name)=="KK" | VARS::post($term_name)=="KS") ? null : '0';
                } else {
                    $this->fields[$term[$j]][]       = $this->data[$term_name];
                    $this->flg_data_ar[$flg[$j]][]   = $this->flg_data[$flg_name];
                    $this->fields[$term[$j]."_DI"][] = $this->attend_data[$term_name];
                }
            }
        }
        $this->cmd     = VARS::request("cmd");
        for ($i=1; $i<=5; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    public function &getMainModel()
    {
        //各科目の講座リストの先頭が空なので科目コンボ変更時はINSERT文を発行しない
        if ($this->cmd == "subclasscd") {
            return;
        }
        $db = Query::dbCheckOut();

        //NO001-->
        $this->testkind["SEM1"] = "0";
        $this->testkind["SEM2"] = "0";
        $this->testkind["SEM3"] = "0";
        $result = $db->query(knjd120kQuery::getSchTest($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            switch ($row["SEMESTER"]) {
                case "1":
                    $this->testkind["SEM1"] = $row["TESTSUM"];
                    break;
                case "2":
                    $this->testkind["SEM2"] = $row["TESTSUM"];
                    break;
                case "3":
                    $this->testkind["SEM3"] = $row["TESTSUM"];
                    break;
                default:
                    break;
            }
        }
        $result->free();
        //NO001<--
        Query::dbCheckIn($db);
    }

    /**
     * 編集完了用のモデルを返します
    */
    public function &getUpdateModel()
    {
        /*  更新処理 */
        knjd120kQuery::update($this);
        $this->setMessage("MSG201");
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
//var_dump($_REQUEST);
