<?php

require_once('for_php7.php');


class graph{

    function CreateDataHidden(&$objForm,$data, $i){

        //���x�� �d�����Ȃ��悤�ɂ�����
        $label = explode(",", $data["label"]);
        $chklbl = array();
        $create = "";
        $cnm = "";
        foreach($label as $key => $val){
            if(!empty($chklbl) && in_array($val, $chklbl)){
                $array = array_count_values($chklbl);
                $cnt = $array[$val];
                $add = "";
                for($a=0;$a<$cnt;$a++){
                    $add .= " ";
                }
                $newval = $val.$add;
            }else{
                $newval = $val;
            }
            $chklbl[] = $val;
            
            $create .= $cnm.$newval;
            $cnm = ",";
        }
        if($create == ""){
            $create = $data["label"];
        }
        knjCreateHidden($objForm, "hogeLabel{$i}", $create);
        
        //�f�[�^
        $cnt = $data["cnt"];
        for($j = 0; $j < $cnt; $j++){
            knjCreateHidden($objForm, "hogeval{$i}{$j}", $data[$j]);
        }
        
        //�}��
        knjCreateHidden($objForm, "hogeCnt{$i}", $cnt);
        if($data["hanrei"] != ""){
            knjCreateHidden($objForm, "hogeHanrei{$i}", $data["hanrei"]);
        }else{
            knjCreateHidden($objForm, "hogeHanrei{$i}");
        }
        
        //�^�C�g��
        knjCreateHidden($objForm, "title{$i}", $data["TITLE"]);
        
        //ymax
        knjCreateHidden($objForm, "ymax{$i}", $data["YMAX"]);
        
        //ymin
        knjCreateHidden($objForm, "ymin{$i}", $data["YMIN"]);
        
        //type
        knjCreateHidden($objForm, "hogeType{$i}", $data["type"]);

        //LineType
        knjCreateHidden($objForm, "hogeLineType{$i}", $data["LineType"]);

        //color
        knjCreateHidden($objForm, "hogeColor{$i}", $data["color"]);
        
        //tooltip�̃��[�h
        knjCreateHidden($objForm, "tooltip{$i}", $data["tooltip"]);
        

    }

}
?>
