<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje071dModel extends Model
{
    public $field      = array();
    public $select_data     = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード
    public $school_name;    //学校名

    /**
     * メンバ変数の初期化
     */
    public function knje071dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    public function init()
    {
        $this->field = array("DATE"           =>  VARS::post("DATE"),            //処理年月日
                            "YEAR"            =>  VARS::post("YEAR"),            //年度
                            "GAKKI"           =>  VARS::post("GAKKI"),           //学期コード
                            "OUTPUT"          =>  VARS::post("OUTPUT"),          //調査書種類
                            "OS"              =>  VARS::post("OS"),              //所見入力時ＯＳ
                            "KANJI"           =>  VARS::post("KANJI"),           //漢字氏名印刷
                            "SONOTAJUUSYO"    =>  VARS::post("SONOTAJUUSYO"),    //その他住所を優先して印字する
                            "MIRISYU"         =>  VARS::post("MIRISYU"),         //未履修科目出力
                            "RISYU"           =>  VARS::post("RISYU"),           //履修のみ科目出力
                            "TANIPRINT_SOUGOU"  => VARS::post("TANIPRINT_SOUGOU"), //総合的な学習の時間の単位を０表示ラジオ
                            "TANIPRINT_RYUGAKU" => VARS::post("TANIPRINT_RYUGAKU"),//留学の単位を０表示ラジオ
                            "FORM6"           =>  VARS::post("FORM6"),           //フォーム選択
                            "useSyojikou3"    =>  VARS::post("useSyojikou3"),    //指導上参考となる諸事欄、３分割フォーム
                            "tyousasyoNotPrintAnotherAttendrec"    =>  VARS::post("tyousasyoNotPrintAnotherAttendrec"),    //出欠の前籍校を含まないチェックボックスを作成する
                            "KOTYO"           =>  VARS::post("KOTYO"),           //校長印刷
                            "HYOTEI"          =>  VARS::post("HYOTEI"),          //評定の処理設定
                            "COMMENT"         =>  VARS::post("COMMENT"),         //学習成績概評
                            "SEKI"            =>  VARS::post("SEKI"),            //記載責任者
                            "DBNAME"          =>  VARS::post("DBNAME")           //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd         = VARS::request("cmd");
        $db = Query::dbCheckOut();
        $query = knje071dQuery::getSchoolName();
        $this->school_name = $db->getOne($query);
        Query::dbCheckIn($db);
        $this->getPropertiesAll();

        //初期値の設定
        $this->Properties["useSyojikou3"]                      = ($this->Properties["useSyojikou3"] == '1')               ? $this->Properties["useSyojikou3"]                      : 0;
        $this->Properties["tyousasyoTaniPrint"]                = ($this->Properties["tyousasyoTaniPrint"] == '2')         ? $this->Properties["tyousasyoTaniPrint"]                : 1;
        $this->Properties["useCertifSchPrintCnt"]              = ($this->Properties["useCertifSchPrintCnt"] == '1')       ? $this->Properties["useCertifSchPrintCnt"]              : 0;
        $this->Properties["tyousasyoAttendrecRemarkFieldSize"] = ($this->Properties["tyousasyoAttendrecRemarkFieldSize"]) ? $this->Properties["tyousasyoAttendrecRemarkFieldSize"] : "";
        $this->Properties["train_ref_1_2_3_field_size"]        = ($this->Properties["train_ref_1_2_3_field_size"])        ? $this->Properties["train_ref_1_2_3_field_size"]        : "";
        $this->Properties["nenYoForm"]                         = ($this->Properties["nenYoForm"])                         ? $this->Properties["nenYoForm"]                         : "6";
        $this->Properties["tyousasyoSougouHyoukaNentani"]      = ($this->Properties["tyousasyoSougouHyoukaNentani"])      ? $this->Properties["tyousasyoSougouHyoukaNentani"]      : "";
        $this->Properties["tyousasyoKinsokuForm"]              = ($this->Properties["tyousasyoKinsokuForm"])              ? $this->Properties["tyousasyoKinsokuForm"]              : "";
    }

    public function &getUpdateModel()
    {
        //発行処理
        $this->printData = array();
        $len = 0;
        $db = Query::dbCheckOut();
        for ($i = 0; $i < get_count($this->select_data["selectdata"]); $i++) {
            $work = explode("-", $this->select_data["selectdata"][$i]);
            $grd = $work[0];
            $certifKindcd = '1' == $grd ? "025" : "008"; // 進学用調査書のみ
            $schregno = $work[1];

            $cQuery = knje071dQuery::getCertifIndex($schregno, $this);
            $updateCount = 0;
            $result2 = $db->query($cQuery);
            while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $certifIndex = $row["CERTIF_INDEX"];
                if ($certifIndex == '') {
                    continue;
                }
                $certif_maxnum = knje071dQuery::get_certif_maxnumchk($db, CTRL_YEAR, $certifIndex); //２度押しチェック
                if ($certif_maxnum == 0) {
                    $certif_no_div = knje071dQuery::getCertifSchool($db, CTRL_YEAR, $certifKindcd);
                    if ($certif_no_div == 0) {
                        $certif_maxnum = knje071dQuery::get_certif_maxnumber($db, CTRL_YEAR, $this, $certifIndex);
                    }
                    knje071dQuery::getUpdateQuery(
                        $db,
                        CTRL_YEAR,
                        $certifIndex,
                        knje071dQuery::getissuername($db, STAFFCD),
                        $certif_maxnum,
                        $this->control["学籍処理日"],
                        $certif_no_div,
                        $this
                    );
                }

                //発行番号の値はプロパティーファイルでそのままにするか、DBの値を使う。
                if ($this->Properties["certifNoSyudou"] != '1') {
                    $work[2] = $certif_maxnum;
                }
                $work[3] = $certifIndex;

                $comma = "";
                $tmp = "";
                for ($j = 0; $j < 4; $j++) {
                    $tmp .= $comma.$work[$j];
                    $comma = "-";
                }
                $this->printData[$len++] = $tmp;
                $updateCount += 1;
            }
            $result2->free();
        }
        Query::dbCheckIn($db);
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
