package com.lagermanagement.space.web.dto.xml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "bestellung")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestellungXml {

    @XmlElement
    private String bestellnummer;

    @XmlElement
    private String lieferantName;

    @XmlElement
    private String artikelnummer;

    @XmlElement
    private String bezeichnung;

    @XmlElement
    private int bestellmenge;

    @XmlElement
    private String einkaufspreis;

    @XmlElement
    private String gesamtbetrag;

    @XmlElement
    private String gewuenschtesLieferdatum;

    @XmlElement
    private String notiz;

    @XmlElement
    private String erstelltVon;

    @XmlElement
    private String erstelltAm;
}
