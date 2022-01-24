<?php
/**
 * 定数定義
*/

class knjh442dModel extends Model
{
    public $field      = array();
    public $cmd;
    public $subclassGroup;
    public $control;        //コントロールマスタ
    public $schoolName;
    public $schoolCode;
    public $firstFlg;  //初期処理フラグ。formの最後で切り替え。
    public $categorySelected3; //ベネッセ
    public $categorySelected4; //スタディサポート
    public $categorySelected5; //駿台
    public $categorySelected6; //河合塾

    /**
     * メンバ変数の初期化
     */
    public function knjh442dModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"                       => VARS::post("YEAR"),                         //年度
                             "SEMESTER"                   => VARS::post("SEMESTER"),                     //学期コンボ
                             "GRADE"                      => VARS::post("GRADE"),                        //学年
                             "TENTATIVE_FLG"              => VARS::post("TENTATIVE_FLG"),                //仮評定含める
                             "CATEGORY_IS_BENESSE_TEST"   => VARS::post("CATEGORY_IS_BENESSE_TEST"),     //ベネッセCheckBox
                             "CATEGORY_IS_STUDY_SUP"      => VARS::post("CATEGORY_IS_STUDY_SUP"),        //スタディサポートCheckBox
                             "CATEGORY_IS_SUNDAI"         => VARS::post("CATEGORY_IS_SUNDAI"),           //駿台模試CheckBox
                             "CATEGORY_IS_KAWAI"          => VARS::post("CATEGORY_IS_KAWAI"),            //河合塾模試CheckBox
                             "PERCENTAGE"                 => VARS::post("PERCENTAGE"),                   //評定の実力に対する割合
                             "PERCENTAGE2"                => "100",                                      //100 - 評定の実力に対する割合
                             "DBNAME"                     => VARS::post("DBNAME")                        //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->categorySelected3 = explode(",", VARS::post("HID_CATEGORY_SELECTED3"));
        $this->categorySelected4 = explode(",", VARS::post("HID_CATEGORY_SELECTED4"));
        $this->categorySelected5 = explode(",", VARS::post("HID_CATEGORY_SELECTED5"));
        $this->categorySelected6 = explode(",", VARS::post("HID_CATEGORY_SELECTED6"));
        $this->getPropertiesAll();

        if ($this->cmd == "") {  //初期処理だけ
            $this->firstFlg = true;
        }
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;

        //学校名取得
        $query = knjh442dQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須項目チェック
        if (!$this->field["PERCENTAGE"]) {
            $msgId = "MSG203";
            $msg .= "\\評定の実力に対する割合が入力されていません。";
            $this->setWarning($msgId, $msg);
            return false;
        }

        //リストtoリスト
        $returnFlg = false;
        if ($this->field["CATEGORY_IS_BENESSE_TEST"] && $this->categorySelected3[0]) {
            $returnFlg = true;
        }
        if ($this->field["CATEGORY_IS_STUDY_SUP"] && $this->categorySelected4[0]) {
            $returnFlg = true;
        }
        if ($this->field["CATEGORY_IS_SUNDAI"] && $this->categorySelected5[0]) {
            $returnFlg = true;
        }
        if ($this->field["CATEGORY_IS_KAWAI"] && $this->categorySelected6[0]) {
            $returnFlg = true;
        }
        if (!$returnFlg) {
            $this->setWarning("MSG916", "");
        }

        return true;
    }

    public function getExecModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入力チェック
        if ($this->validateInput()) {
            $this->field["PERCENTAGE2"] = 100 - $this->field["PERCENTAGE"];

            //更新 -- AFT_RECOMMENDATION_RANK_HEAD_DAT
            knjh442dQuery::getUpdateHead($db, $this);

            //削除 -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
            knjh442dQuery::getDeleteRank($db, $this);

            if ($this->field["CATEGORY_IS_BENESSE_TEST"] && $this->categorySelected3[0]) {
                $mockCdArr = array();
                $kokugoArr = array();
                $sugakuArr = array();
                $eigoArr = array();
                foreach ($this->categorySelected3 as $key => $value) {
                    list($cd, $year, $ghosyaCd, $grade, $mockNo) = explode("-", $value);
                    $mockCdArr[] = str_replace("-", "", $value);
                    $subclassCd = $mockNo."0".substr($this->field["GRADE"], 1, 1); // 模試番号 + '0'(種別コード)
                    $kokugoArr[] = $subclassCd."001";
                    $sugakuArr[] = $subclassCd."002";
                    $eigoArr[] = $subclassCd."003";
                }
                //更新 -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
                knjh442dQuery::getUpdateRank($db, $this, $mockCdArr, $kokugoArr, $sugakuArr, $eigoArr);
            }
    
            if ($this->field["CATEGORY_IS_STUDY_SUP"] && $this->categorySelected4[0]) {
                $mockCdArr = array();
                $kokugoArr = array();
                $sugakuArr = array();
                $eigoArr = array();
                foreach ($this->categorySelected4 as $key => $value) {
                    list($cd, $year, $ghosyaCd, $grade, $mockNo) = explode("-", $value);
                    $mockCdArr[] = str_replace("-", "", $value);
                    $subclassCd = $mockNo."0".substr($this->field["GRADE"], 1, 1); // 模試番号 + '0'(種別コード)
                    $kokugoArr[] = $subclassCd."001";
                    $sugakuArr[] = $subclassCd."002";
                    $eigoArr[] = $subclassCd."003";
                }
                //更新 -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
                knjh442dQuery::getUpdateRank($db, $this, $mockCdArr, $kokugoArr, $sugakuArr, $eigoArr);
            }
    
            if ($this->field["CATEGORY_IS_SUNDAI"] && $this->categorySelected5[0]) {
                $mockCdArr = array();
                $kokugoArr = array();
                $sugakuArr = array();
                $eigoArr = array();
                foreach ($this->categorySelected5 as $key => $value) {
                    list($cd, $year, $ghosyaCd, $grade, $mockNo) = explode("-", $value);
                    $mockCdArr[] = str_replace("-", "", $value);
                    $subclassCd = $mockNo."0".substr($this->field["GRADE"], 1, 1); // 模試番号 + '0'(種別コード)
                    $kokugoArr[] = $subclassCd."001";
                    $sugakuArr[] = $subclassCd."002";
                    $eigoArr[] = $subclassCd."003";
                }
                //更新 -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
                knjh442dQuery::getUpdateRank($db, $this, $mockCdArr, $kokugoArr, $sugakuArr, $eigoArr);
            }
            
            if ($this->field["CATEGORY_IS_KAWAI"] && $this->categorySelected6[0]) {
                $mockCdArr = array();
                $kokugoArr = array();
                $sugakuArr = array();
                $eigoArr = array();
                foreach ($this->categorySelected6 as $key => $value) {
                    list($cd, $year, $ghosyaCd, $grade, $mockNo) = explode("-", $value);
                    $mockCdArr[] = str_replace("-", "", $value);
                    $subclassCd = $mockNo."0".substr($this->field["GRADE"], 1, 1); // 模試番号 + '0'(種別コード)
                    $kokugoArr[] = $subclassCd."001";
                    $sugakuArr[] = $subclassCd."002";
                    $eigoArr[] = $subclassCd."003";
                }
                //更新 -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
                knjh442dQuery::getUpdateRank($db, $this, $mockCdArr, $kokugoArr, $sugakuArr, $eigoArr);
            }

            //更新(順位) -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
            knjh442dQuery::getUpdateRank2($db, $this);
            $this->setMessage("MSG201");
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
