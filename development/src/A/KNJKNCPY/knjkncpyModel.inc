<?php

require_once('for_php7.php');

class knjkncpyModel extends Model {

    var $cmd;
    var $hash;      //検証用のハッシュを収める

    function knjkncpyModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");
        //プロパティファイルの読込み
        $this->getPropertiesAll();

    }

    //署名されたデータの検証、更新
    function &getShomeiModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //表示データと現在データのHASHを比較
        $query = knjkncpyQuery::getAttestWk("CHAGE");
        $this->update($db, $query);
        $query = knjkncpyQuery::getAttestWk("LAST");
        $this->update($db, $query);

        $this->setMessage("データ作成が完了しました。");

        $db->commit();
        Query::dbCheckIn($db);

    }

    //アップデート
    function &update($db, $seitoQuery) {

        $resultSeito = $db->query($seitoQuery);
        while ($setData = $resultSeito->fetchRow(DB_FETCHMODE_ASSOC)) {
            //HTRAINREMARK_DAT
            $query = knjkncpyQuery::getTrainRow($setData);
            $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //SCHREG_STUDYREC_DAT
            $query = knjkncpyQuery::getStudyRec($setData);
            $result = $db->query($query);
            $study = "";
	        //教育課程対応
	        if ($this->Properties["useCurriculumcd"] == '1') {
	            while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	                $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	                          $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	            }
            } else {
	            while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	                $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	                          $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	            }
            }
            $result->free();

            //SCHREG_ATTENDREC_DAT
            $attend = $db->getRow(knjkncpyQuery::getAttendRec($setData), DB_FETCHMODE_ASSOC);

            //現在データのHASHを取得
            $makeHash = $this->makeHash($tRow, $study, $attend);

            $updQuery = knjkncpyQuery::getUpdSql($setData, $makeHash);
            $db->query($updQuery);
        }
        $resultSeito->free();

        return true;
    }

    //ハッシュ値を作る
    function makeHash($tRow, $study, $attend) {

        $data = $tRow["TOTALSTUDYACT"].$tRow["TOTALSTUDYVAL"].$tRow["SPECIALACTREMARK"].$tRow["TOTALREMARK"].$tRow["ATTENDREC_REMARK"].
                $study.$attend["CLASSDAYS"].$attend["OFFDAYS"].$attend["ABSENT"].$attend["SUSPEND"].$attend["MOURNING"].
                $attend["ABROAD"].$attend["REQUIREPRESENT"].$attend["SICK"].$attend["ACCIDENTNOTICE"].$attend["NOACCIDENTNOTICE"].$attend["PRESENT"];

        return sha1($data);
    }
}
?>
