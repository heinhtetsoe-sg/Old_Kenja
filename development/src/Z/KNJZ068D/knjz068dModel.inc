<?php

require_once('for_php7.php');

class knjz068dModel extends Model {

    var $cmd;
    var $control_data = array();        //コントロールのデータを保持
    var $field;

    function knjz068dModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
        $this->field = array();
    }

    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");

        $this->field["IBYEAR"] = VARS::post("IBYEAR");
        $this->field["IBGRADE"] = VARS::post("IBGRADE");
        $this->field["IBPRG_COURSE"] = VARS::post("IBPRG_COURSE");
        $this->field["IBSUBCLASSCD"] = VARS::post("IBSUBCLASSCD");

        //チェックボックス
        $this->updCheckBox = array();
        if (is_array($this->checkBoxName)) {
            foreach ($this->checkBoxName as $key => $val) {
                if (VARS::post($val)) {
                    $this->updCheckBox[] = VARS::post($val);
                }
            }
        }

    }

    /* 入力項目チェック */
    function validateInput()
    {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $this->delete($db);

            foreach ($this->updCheckBox as $key => $val) {
                list($div1, $div2, $mark, $seq, $viewCd) = preg_split("/_/", $val);
                $this->insert($db, $div1, $div2, $mark, $seq, $viewCd);
            }

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
        }
        return;
    }

    /* 削除 */
    function &delete($db) {
        $query = knjz068dQuery::getDeleteSql($this);
        $db->query($query);
    }

    /* 新規 */
    function &insert($db, $div1, $div2, $mark, $seq, $viewCd)
    {
        $query = knjz068dQuery::getInsertSql($this, $div1, $div2, $mark, $seq, $viewCd);
        $db->query($query);
    }

    
    //CVSファイル
    //サーバ ⇒ クライアント
    function getDownloadCsvModel() {
        $db = Query::dbCheckOut();
        //ヘッダー情報を取得する
        $setLabel = "";
        $setConma = "";
        $setTitleKyoutu = "";
        $count=0;
        $lf = "\r\n";   //改行
        
        //対象データのリストを取得
        $query = knjz068dQuery::getIbUnitDatList($this);
        $result = $db->query($query);
        while ($listrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //タイトル(横)
            $titleYoko = setTitleYokoModel($this, $db, $listrow);
            //タイトル(縦)
            $titleTate = setTitleTateModel($this, $db, $listrow);
            foreach ($titleTate as $tkey => $tVal) {
                foreach ($titleYoko as $yKey => $yVal) {
                    foreach ($yVal["VIEWCD"] as $key => $val) {
                        //ヘッダーセット
                        if ($setIbKey !== $tVal["IBYEAR"]."-".$tVal["IBGRADE"]."-".$tVal["IBCLASSCD"]."-".$tVal["IBPRG_COURSE"]."-".$tVal["IBCURRICULUM_CD"]."-".$tVal["IBSUBCLASSCD"]) {
                            //データが異なるため、最初は改行する
                            if ($count > 0) {
                                $contents .= $lf;
                            }
                            $setConma = ",,,,,,,,,";
                            $setTitleKyoutu = "年度,学年,IB科目,,,,評価区分1,評価区分2,評価基準,";
                            //タイトルデータを取得し、ループでまわす
                            $setSemester = "";
                            $setIbSubClass = "";
                            $setUnit = "";
                            $setViewcd = "";
                            $conma = "";
                            $query = knjz068dQuery::getIbUnit($this, $tVal);
                            $resultHeader = $db->query($query);
                            $viewNumber = 1;
                            while ($row = $resultHeader->fetchRow(DB_FETCHMODE_ASSOC)) {
                                //IBSEQが異なるとき
                                if ($checkSeq != $row["IBSEQ"]) {
                                    $setSemester    .= $conma.$row["SEMESTERNAME"];
                                    $setIbSubClass  .= $conma.$row["SUBCLASSNAME"];
                                    $setUnit        .= $conma."Task".$row["IBSEQ"];
                                    $viewNumber = 1;
                                }
                                $conma = ",";
                                //IBSEQが同じとき
                                if ($checkSeq == $row["IBSEQ"]) {
                                    $setSemester    .= $conma;
                                    $setIbSubClass  .= $conma;
                                    $setUnit        .= $conma;
                                }
                                $checkSeq = $row["IBSEQ"];
                                $viewNumber++;
                            }
                            $resultHeader->free();
                            $contents .= $setConma.$setSemester.$lf;
                            $contents .= $setConma.$setIbSubClass.$lf;
                            $contents .= $setTitleKyoutu.$setUnit;
                        }

                        //データセット
                        if ($setLabel !== $tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"]) {
                            $contents .= $lf;
                            $contents .= $tVal["IBYEAR"];
                            $contents .= ",".$tVal["IBGRADE"];
                            $contents .= ",".$tVal["IBCLASSCD"];
                            $contents .= ",".$tVal["IBPRG_COURSE"];
                            $contents .= ",".$tVal["IBCURRICULUM_CD"];
                            $contents .= ",".$tVal["IBSUBCLASSCD"];
                            $contents .= ",".$tVal["DIV1_NAME"];
                            if ($tVal["IBEVAL_DIV1"] == "1") {
                                $contents .= ",".$tVal["Z037_NAME"];
                            } else {
                                $contents .= ",".$tVal["Z038_NAME"];
                            }
                            $contents .= ",".$tVal["IBEVAL_MARK"];
                        }
                        $setIbKey = $tVal["IBYEAR"]."-".$tVal["IBGRADE"]."-".$tVal["IBCLASSCD"]."-".$tVal["IBPRG_COURSE"]."-".$tVal["IBCURRICULUM_CD"]."-".$tVal["IBSUBCLASSCD"];
                        $setIbSubclasscd = $tVal["IBCLASSCD"]."-".$tVal["IBPRG_COURSE"]."-".$tVal["IBCURRICULUM_CD"]."-".$tVal["IBSUBCLASSCD"];
                        $query = knjz068dQuery::getUnitPlanCnt($this, $tVal["IBEVAL_DIV1"], $tVal["IBEVAL_DIV2"], $tVal["IBEVAL_MARK"], $yVal["IBSEQ"], $tVal["IBGRADE"], $setIbSubclasscd);
                        $checkCnt = $db->getOne($query);
                        if ($checkCnt > 0) {
                            $contents .= ",".'1';
                        } else {
                            $contents .= ",".'';
                        }
                        $setLabel = $tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"];
                        $count++;
                    }
                }
                $data = true;
            }
        }
        $result->free();
        
        Query::dbCheckIn($db);
        if ($data){
            $couse="IB評価基準_学習指導要領_観点別評価対応一覧(DP)";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        }
        else {
            $this->setWarning("MSG303");
            return false;
        }
        
    }

}

//タイトル(横)
function setTitleYokoModel($this, $db, $listrow) {
    $query = knjz068dQuery::getIbUnit($this, $listrow);
    $result = $db->query($query);

    $titleYoko = array();
    $titleYokoCnt = 0;
    $titleYokoBefSeq = "";

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($titleYokoBefSeq != "" && $titleYokoBefSeq != $row["IBSEQ"]) {
            $titleYokoCnt++;
        }
        $titleYokoBefSeq = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["IBSEQ"] = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["SEMESTER"] = $row["SEMESTERNAME"];
        $titleYoko[$titleYokoCnt]["SUBCLASS"] = $row["SUBCLASSNAME"];
        $titleYoko[$titleYokoCnt]["VIEWCD"][] = $row["VIEWCD"];
    }
    $result->free();

    return $titleYoko;
}

//タイトル(縦)
function setTitleTateModel($this, $db, $listrow) {
    $titleTate = array();
    $titleTateHenSyuu = array();
    $tateCnt = 0;

    $query = knjz068dQuery::getIbViewName($this, $listrow);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $titleTate[$tateCnt]["IBYEAR"] = $row["IBYEAR"];
        $titleTate[$tateCnt]["IBGRADE"] = $row["IBGRADE"];
        $titleTate[$tateCnt]["IBCLASSCD"] = $row["IBCLASSCD"];
        $titleTate[$tateCnt]["IBPRG_COURSE"] = $row["IBPRG_COURSE"];
        $titleTate[$tateCnt]["IBCURRICULUM_CD"] = $row["IBCURRICULUM_CD"];
        $titleTate[$tateCnt]["IBSUBCLASSCD"] = $row["IBSUBCLASSCD"];
        $titleTate[$tateCnt]["IBSUBCLASSCD_SET"] = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"];
        $titleTate[$tateCnt]["IBPERFECT"] = $row["IBPERFECT"];

        $titleTate[$tateCnt]["DIV1_NAME"] = $row["Z035_NAME"];
        //$row["IBEVAL_DIV1"] == "1"のとき
        $titleTate[$tateCnt]["Z037_NAME"] = $row["Z037_NAME"];
        //$row["IBEVAL_DIV1"] == "2"のとき
        $titleTate[$tateCnt]["Z038_NAME"] = $row["Z038_NAME"];
        $titleTate[$tateCnt]["IBEVAL_MARK"] = $row["IBEVAL_MARK"];
        $titleTate[$tateCnt]["IBEVAL_DIV1"] = $row["IBEVAL_DIV1"];
        $titleTate[$tateCnt]["IBEVAL_DIV2"] = $row["IBEVAL_DIV2"];
        $tateCnt++;
    }
    $result->free();

    return $titleTate;
}
?>
